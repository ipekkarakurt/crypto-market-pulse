package com.cryptomarketpulse.service;

import com.cryptomarketpulse.model.Candle;
import com.cryptomarketpulse.model.Trade;
import com.cryptomarketpulse.repository.CandleRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Profile("processing")
public class CandleService {

    private static final int MAX_OPTIMISTIC_RETRIES = 3;
    private static final long BASE_BACKOFF_MS = 10L;

    private final CandleRepository candleRepository;
    private final TransactionTemplate requiresNewTx;

    public CandleService(CandleRepository candleRepository, PlatformTransactionManager transactionManager) {
        this.candleRepository = candleRepository;
        this.requiresNewTx = new TransactionTemplate(Objects.requireNonNull(transactionManager));
        this.requiresNewTx.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
    }

    public Candle aggregateTrade(Trade trade) {
        Trade input = Objects.requireNonNull(trade, "trade must not be null");
        ObjectOptimisticLockingFailureException lastException = null;

        for (int attempt = 1; attempt <= MAX_OPTIMISTIC_RETRIES; attempt++) {
            try {
                return requiresNewTx.execute(status -> aggregateInCurrentTransaction(input));
            } catch (ObjectOptimisticLockingFailureException ex) {
                lastException = ex;
                if (attempt == MAX_OPTIMISTIC_RETRIES) {
                    break;
                }
                sleepBackoff(attempt);
            }
        }

        throw Objects.requireNonNull(lastException, "optimistic retry failed without exception");
    }

    public List<Candle> findRecent(String symbol, int limit) {
        return candleRepository.findBySymbolOrderByStartTimeDesc(symbol, PageRequest.of(0, limit));
    }

    private Candle aggregateInCurrentTransaction(Trade trade) {
        Instant windowStart = trade.getTradeTime().truncatedTo(ChronoUnit.MINUTES);
        Candle candle = candleRepository.findBySymbolAndStartTime(trade.getSymbol(), windowStart)
                .orElseGet(() -> new Candle(
                        trade.getSymbol(),
                        windowStart,
                        trade.getPrice(),
                        trade.getPrice(),
                        trade.getPrice(),
                        trade.getPrice(),
                        trade.getQuantity(),
                        1));
        if (candle.getId() != null) {
            candle.applyTrade(trade.getPrice(), trade.getQuantity());
        }
        return candleRepository.saveAndFlush(candle);
    }

    private void sleepBackoff(int attempt) {
        try {
            Thread.sleep(BASE_BACKOFF_MS * attempt);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during optimistic-lock retry backoff", interrupted);
        }
    }
}
