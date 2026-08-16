package com.cryptomarketpulse.dto;

import com.cryptomarketpulse.model.Trade;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record TradeResponse(
        Long id,
        String symbol,
        BigDecimal price,
        BigDecimal quantity,
        Instant timestamp) {

    /** Only persisted trades are exposed, so the id is required here. */
    public static TradeResponse from(Trade trade) {
        return new TradeResponse(
                Objects.requireNonNull(trade.getId(), "trade must be saved before it is returned"),
                trade.getSymbol(),
                trade.getPrice(),
                trade.getQuantity(),
                trade.getTimestamp());
    }
}
