package com.cryptomarketpulse.exception;

public class TradeNotFoundException extends RuntimeException {

    public TradeNotFoundException(Long id) {
        super("Trade not found with id: " + id);
    }
}
