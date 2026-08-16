package com.cryptomarketpulse.controller;

import com.cryptomarketpulse.dto.TradeResponse;
import com.cryptomarketpulse.service.TradeService;
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

    private final TradeService tradeService;

    public MarketController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/{symbol}/trades")
    public List<TradeResponse> getTrades(
            @PathVariable
                    @Pattern(regexp = "^BTC-USD$", message = "only BTC-USD is supported at this stage")
                    String symbol,
            @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int limit) {
        return tradeService.findRecent(symbol, limit).stream().map(TradeResponse::from).toList();
    }
}
