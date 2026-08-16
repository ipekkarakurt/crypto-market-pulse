package com.cryptomarketpulse.repository;

import com.cryptomarketpulse.model.Trade;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTradeRepository implements TradeRepository {

    /** Descending key order, so iteration yields the newest trades first. */
    private final NavigableMap<Long, Trade> trades =
            new ConcurrentSkipListMap<>(Comparator.<Long>reverseOrder());

    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Trade save(Trade trade) {
        Long id = idSequence.getAndIncrement();
        Trade saved = new Trade(
                id,
                trade.getSymbol(),
                trade.getPrice(),
                trade.getQuantity(),
                trade.getTimestamp());
        trades.put(id, saved);
        return saved;
    }

    @Override
    public List<Trade> findRecent(String symbol, int limit) {
        if (symbol == null) {
            return trades.values().stream().limit(limit).toList();
        }
        return trades.values().stream()
                .filter(trade -> symbol.equals(trade.getSymbol()))
                .limit(limit)
                .toList();
    }

    @Override
    public Optional<Trade> findById(Long id) {
        return Optional.ofNullable(trades.get(id));
    }
}
