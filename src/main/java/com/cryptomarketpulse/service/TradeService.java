package com.cryptomarketpulse.service;

import com.cryptomarketpulse.dto.CreateTradeRequest;
import com.cryptomarketpulse.exception.TradeNotFoundException;
import com.cryptomarketpulse.model.Trade;
import com.cryptomarketpulse.repository.TradeRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @Transactional
    public Trade create(CreateTradeRequest request) {
        Trade trade = new Trade(
                normalize(request.getSymbol()),
                request.getPrice(),
                request.getQuantity(),
                Instant.now());
        return tradeRepository.save(trade);
    }

    @Transactional(readOnly = true)
    public List<Trade> findRecent(String symbol, int limit) {
        String normalized = normalize(symbol);
        if (normalized == null) {
            return tradeRepository.findAllByOrderByTradeTimeDesc(PageRequest.of(0, limit));
        }
        return tradeRepository.findBySymbolOrderByTradeTimeDesc(normalized, PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public Trade findById(Long id) {
        return tradeRepository.findById(id).orElseThrow(() -> new TradeNotFoundException(id));
    }

    /** Locale.ROOT keeps "i" from becoming "İ" on Turkish-locale machines. */
    private String normalize(String symbol) {
        return symbol == null ? null : symbol.toUpperCase(Locale.ROOT);
    }
}
