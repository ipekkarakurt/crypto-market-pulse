package com.cryptomarketpulse.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;

@Getter
public class Trade {
    private final String symbol;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final Instant tradeTime;

    public Trade(String symbol, BigDecimal price, BigDecimal quantity, Instant tradeTime) {
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.tradeTime = tradeTime;
    }
}
