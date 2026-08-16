package com.cryptomarketpulse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "candles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Candle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 16)
    private String symbol;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal open;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal high;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal low;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal close;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal volume;

    @Column(name = "trade_count", nullable = false)
    private long tradeCount;

    @Version
    @Column(nullable = false)
    private long version;

    public Candle(
            String symbol,
            Instant startTime,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            BigDecimal volume,
            long tradeCount) {
        this.symbol = symbol;
        this.startTime = startTime;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.tradeCount = tradeCount;
    }

    public void applyTrade(BigDecimal price, BigDecimal quantity) {
        if (price.compareTo(high) > 0) {
            high = price;
        }
        if (price.compareTo(low) < 0) {
            low = price;
        }
        close = price;
        volume = volume.add(quantity);
        tradeCount += 1;
    }
}
