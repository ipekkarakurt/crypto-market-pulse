package com.cryptomarketpulse.service;

import com.cryptomarketpulse.dto.CreateTradeRequest;
import com.cryptomarketpulse.exception.TradeNotFoundException;
import com.cryptomarketpulse.model.Trade;
import com.cryptomarketpulse.repository.TradeRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public Trade create(CreateTradeRequest request) {
        Trade trade = new Trade(
                null,
                normalize(request.getSymbol()),
                request.getPrice(),
                request.getQuantity(),
                Instant.now());
        return tradeRepository.save(trade);
    }

    public List<Trade> findRecent(String symbol, int limit) {
        return tradeRepository.findRecent(normalize(symbol), limit);
    }

    public Trade findById(Long id) {
        return tradeRepository.findById(id).orElseThrow(() -> new TradeNotFoundException(id));
    }

    /** Locale.ROOT keeps "i" from becoming "İ" on Turkish-locale machines. */
    private String normalize(String symbol) {
        return symbol == null ? null : symbol.toUpperCase(Locale.ROOT);
    }
}
