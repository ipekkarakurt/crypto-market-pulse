package com.cryptomarketpulse.controller;

import com.cryptomarketpulse.dto.CandleResponse;
import com.cryptomarketpulse.service.CandleService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/markets")
public class MarketController {

    private final CandleService candleService;

    public MarketController(CandleService candleService) {
        this.candleService = candleService;
    }

    @GetMapping("/{symbol}/candles")
    public List<CandleResponse> getCandles(
            @PathVariable
                    @Pattern(regexp = "^BTC-USD$", message = "only BTC-USD is supported at this stage")
                    String symbol,
            @RequestParam @Pattern(regexp = "^1m$", message = "only 1m interval is supported")
                    String interval,
            @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int limit) {
        return candleService.findRecent(symbol, limit).stream().map(CandleResponse::from).toList();
    }
}
