package com.cryptomarketpulse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTradeRequest {

    @NotBlank
    @Pattern(regexp = "^(?i)(BTC|ETH|SOL)-USD$", message = "must be BTC-USD, ETH-USD, or SOL-USD")
    private String symbol;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 12, fraction = 8)
    private BigDecimal price;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 12, fraction = 8)
    private BigDecimal quantity;
}
