package com.cryptomarketpulse.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        Map<String, String> fields) {

    public static ErrorResponse of(int status, String error) {
        return new ErrorResponse(Instant.now(), status, error, null);
    }

    public static ErrorResponse of(int status, String error, Map<String, String> fields) {
        return new ErrorResponse(Instant.now(), status, error, fields);
    }
}
