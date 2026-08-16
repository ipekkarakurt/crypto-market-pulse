package com.cryptomarketpulse.service;

import com.cryptomarketpulse.config.CoinbaseWebSocketProperties;
import com.cryptomarketpulse.dto.CoinbaseMatchMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CoinbaseWebSocketIngestionService {

    private final TradeService tradeService;
    private final CoinbaseWebSocketProperties properties;
    private final ObjectMapper objectMapper;
    private final WebSocketClient webSocketClient = new ReactorNettyWebSocketClient();
    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor(r -> new Thread(r, "coinbase-ws-ingestion"));
    private final AtomicLong lastSequence = new AtomicLong(-1L);

    private volatile boolean running;

    public CoinbaseWebSocketIngestionService(
            TradeService tradeService,
            CoinbaseWebSocketProperties properties,
            ObjectMapper objectMapper) {
        this.tradeService = tradeService;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @jakarta.annotation.PostConstruct
    void start() {
        if (!properties.isEnabled()) {
            log.info("Coinbase WebSocket ingestion disabled by config");
            return;
        }

        running = true;
        executorService.submit(this::connectLoop);
    }

    @jakarta.annotation.PreDestroy
    void stop() {
        running = false;
        executorService.shutdownNow();
    }

    private void connectLoop() {
        int reconnectAttempts = 0;
        while (running) {
            try {
                connectAndConsume();
                reconnectAttempts = 0;
            } catch (Exception ex) {
                log.warn("Coinbase WebSocket disconnected: {}", ex.getMessage());
                reconnectAttempts++;
            }

            if (!running) {
                break;
            }

            try {
                long sleepMs = calculateReconnectDelayMs(reconnectAttempts);
                log.info(
                        "Reconnecting to Coinbase in {} ms (attempt={})",
                        sleepMs,
                        reconnectAttempts);
                Thread.sleep(sleepMs);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void connectAndConsume() throws Exception {
        URI uri = URI.create(properties.getUrl());
        String subscribeMessage = objectMapper.writeValueAsString(Map.of(
                "type", "subscribe",
                "product_ids", new String[] {properties.getSymbol()},
                "channels", new String[] {"matches"}));

        log.info("Connecting Coinbase WebSocket: {}", properties.getUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Sec-WebSocket-Extensions", "permessage-deflate");

        webSocketClient.execute(uri, headers, session -> {
                    Mono<Void> sendSubscription =
                            session.send(Mono.just(session.textMessage(subscribeMessage)));

                    Mono<Void> receiveMessages = session.receive()
                            .map(message -> message.getPayloadAsText())
                            .doOnNext(this::handleMessage)
                            .then();

                    return sendSubscription.then(receiveMessages);
                })
                .block();
    }

    private void handleMessage(String payload) {
        try {
            CoinbaseMatchMessage message = objectMapper.readValue(payload, CoinbaseMatchMessage.class);
            if (!"match".equals(message.type())) {
                return;
            }

            String symbol = message.productId();
            if (!properties.getSymbol().equals(symbol)) {
                return;
            }

            if (!isValidSequence(message.sequence(), symbol)) {
                return;
            }

            BigDecimal price = new BigDecimal(message.price());
            BigDecimal quantity = new BigDecimal(message.quantity());
            Instant tradeTime = Instant.parse(message.time());

            tradeService.ingestTrade(symbol, price, quantity, tradeTime);
        } catch (Exception ex) {
            log.debug("Skipping unparsable websocket message: {}", ex.getMessage());
        }
    }

    private boolean isValidSequence(Long incomingSequence, String symbol) {
        if (incomingSequence == null) {
            return true;
        }

        long previous = lastSequence.get();
        if (previous == -1L) {
            lastSequence.set(incomingSequence);
            return true;
        }

        if (incomingSequence <= previous) {
            log.debug(
                    "Ignoring out-of-order/duplicate message for {}. incoming={}, previous={}",
                    symbol,
                    incomingSequence,
                    previous);
            return false;
        }

        if (incomingSequence > previous + 1) {
            log.warn(
                    "Sequence gap detected for {}. incoming={}, expected={}",
                    symbol,
                    incomingSequence,
                    previous + 1);
        }

        lastSequence.set(incomingSequence);
        return true;
    }

    private long calculateReconnectDelayMs(int reconnectAttempts) {
        long base = Math.max(1L, properties.getReconnectDelay().toMillis());
        long max = Math.max(base, properties.getMaxReconnectDelay().toMillis());
        long exponential = base;
        for (int i = 0; i < reconnectAttempts && exponential < max; i++) {
            if (exponential > Long.MAX_VALUE / 2) {
                exponential = max;
                break;
            }
            exponential = Math.min(max, exponential * 2);
        }

        double jitterFactor = Math.max(0.0d, properties.getReconnectJitterFactor());
        long jitterRange = (long) (exponential * jitterFactor);
        if (jitterRange == 0L) {
            return exponential;
        }

        long jitter = ThreadLocalRandom.current().nextLong(-jitterRange, jitterRange + 1);
        long withJitter = exponential + jitter;
        return Math.max(base, Math.min(max, withJitter));
    }
}
