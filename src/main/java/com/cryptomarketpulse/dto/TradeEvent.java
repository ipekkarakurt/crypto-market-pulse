package com.cryptomarketpulse.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeEvent(
        String symbol,
        BigDecimal price,
        BigDecimal quantity,
        Instant tradeTime,
        Long sequence) {}
