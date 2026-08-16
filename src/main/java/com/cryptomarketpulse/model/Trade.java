package com.cryptomarketpulse.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Trade {

    /** Null until the repository assigns one on save. */
    private final Long id;

    private final String symbol;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final Instant timestamp;
}
