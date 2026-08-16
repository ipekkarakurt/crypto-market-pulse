package com.cryptomarketpulse.repository;

import com.cryptomarketpulse.model.Candle;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandleRepository extends JpaRepository<Candle, Long> {

    Optional<Candle> findBySymbolAndStartTime(String symbol, Instant startTime);

    List<Candle> findBySymbolOrderByStartTimeDesc(String symbol, Pageable pageable);
}
