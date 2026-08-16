package com.cryptomarketpulse.dto;

import com.cryptomarketpulse.model.Candle;
import java.math.BigDecimal;
import java.time.Instant;

public record CandleResponse(
        String symbol,
        Instant start,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal volume,
        long tradeCount) {

    public static CandleResponse from(Candle candle) {
        return new CandleResponse(
                candle.getSymbol(),
                candle.getStartTime(),
                candle.getOpen(),
                candle.getHigh(),
                candle.getLow(),
                candle.getClose(),
                candle.getVolume(),
                candle.getTradeCount());
    }
}
