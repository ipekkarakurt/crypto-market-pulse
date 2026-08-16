package com.cryptomarketpulse.repository;

import com.cryptomarketpulse.model.Trade;
import java.util.List;
import java.util.Optional;

public interface TradeRepository {

    Trade save(Trade trade);

    /** Newest first. A null symbol means every symbol. */
    List<Trade> findRecent(String symbol, int limit);

    Optional<Trade> findById(Long id);
}
