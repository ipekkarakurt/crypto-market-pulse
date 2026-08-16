package com.cryptomarketpulse.service;

import com.cryptomarketpulse.model.Candle;
import com.cryptomarketpulse.model.Trade;
import com.cryptomarketpulse.repository.CandleRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandleService {

    private final CandleRepository candleRepository;
    private final ConcurrentHashMap<String, ReentrantLock> keyLocks = new ConcurrentHashMap<>();

    public CandleService(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    @Transactional
    public Candle aggregateTrade(Trade trade) {
        Instant windowStart = trade.getTradeTime().truncatedTo(ChronoUnit.MINUTES);
        String lockKey = trade.getSymbol() + "|" + windowStart;
        ReentrantLock lock = keyLocks.computeIfAbsent(lockKey, ignored -> new ReentrantLock());
        lock.lock();
        try {
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
            return candleRepository.save(candle);
        } finally {
            lock.unlock();
        }
    }

    @Transactional(readOnly = true)
    public List<Candle> findRecent(String symbol, int limit) {
        return candleRepository.findBySymbolOrderByStartTimeDesc(symbol, PageRequest.of(0, limit));
    }
}
