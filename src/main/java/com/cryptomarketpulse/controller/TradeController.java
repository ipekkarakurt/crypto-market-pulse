package com.cryptomarketpulse.controller;

import com.cryptomarketpulse.dto.CreateTradeRequest;
import com.cryptomarketpulse.dto.TradeResponse;
import com.cryptomarketpulse.service.TradeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trades")
@Validated
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    public ResponseEntity<TradeResponse> create(@Valid @RequestBody CreateTradeRequest request) {
        TradeResponse response = TradeResponse.from(tradeService.create(request));
        URI location = URI.create("/trades/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<TradeResponse> findRecent(
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "50") @Min(1) @Max(1000) int limit) {
        return tradeService.findRecent(symbol, limit).stream()
                .map(TradeResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public TradeResponse findById(@PathVariable Long id) {
        return TradeResponse.from(tradeService.findById(id));
    }
}
