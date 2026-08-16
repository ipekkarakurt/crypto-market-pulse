package com.cryptomarketpulse.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoinbaseMatchMessage(
        String type,
        Long sequence,
        @JsonProperty("product_id") String productId,
        String price,
        @JsonProperty("size") String quantity,
        String time) {}
