package com.cryptomarketpulse.repository;

import com.cryptomarketpulse.model.Trade;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findBySymbolOrderByTradeTimeDesc(String symbol, Pageable pageable);

    List<Trade> findAllByOrderByTradeTimeDesc(Pageable pageable);
}
