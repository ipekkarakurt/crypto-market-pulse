package com.cryptomarketpulse.service;

import com.cryptomarketpulse.dto.TradeEvent;
import com.cryptomarketpulse.model.Trade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("processing")
public class TradeEventConsumer {

    private final CandleService candleService;

    public TradeEventConsumer(CandleService candleService) {
        this.candleService = candleService;
    }

    @KafkaListener(topics = "${app.kafka.market-trades-topic}")
    public void consume(
            TradeEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        candleService.aggregateTrade(new Trade(event.symbol(), event.price(), event.quantity(), event.tradeTime()));
        log.info(
                "Consumed trade event partition={} offset={} symbol={} price={}",
                partition,
                offset,
                event.symbol(),
                event.price());
    }
}
