package com.cryptomarketpulse.service;

import com.cryptomarketpulse.exception.TradeNotFoundException;
import com.cryptomarketpulse.model.Trade;
import com.cryptomarketpulse.repository.TradeRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("processing")
public class TradeService {

    private final TradeRepository tradeRepository;
    private final CandleService candleService;

    public TradeService(TradeRepository tradeRepository, CandleService candleService) {
        this.tradeRepository = tradeRepository;
        this.candleService = candleService;
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
        Long tradeId = Objects.requireNonNull(id, "id must not be null");
        return tradeRepository.findById(tradeId).orElseThrow(() -> new TradeNotFoundException(tradeId));
    }

    @Transactional
    public Trade ingestTrade(String symbol, BigDecimal price, BigDecimal quantity, Instant tradeTime) {
        Trade trade = new Trade(normalize(symbol), price, quantity, tradeTime);
        candleService.aggregateTrade(trade);
        return trade;
    }

    /** Locale.ROOT keeps "i" from becoming "İ" on Turkish-locale machines. */
    private String normalize(String symbol) {
        return symbol == null ? null : symbol.toUpperCase(Locale.ROOT);
    }
}
