package com.cryptomarketpulse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cryptomarketpulse.dto.CreateTradeRequest;
import com.cryptomarketpulse.exception.TradeNotFoundException;
import com.cryptomarketpulse.model.Trade;
import com.cryptomarketpulse.repository.InMemoryTradeRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TradeServiceTest {

    private TradeService tradeService;

    @BeforeEach
    void setUp() {
        tradeService = new TradeService(new InMemoryTradeRepository());
    }

    @Test
    void createAssignsIncrementalIds() {
        Trade first = tradeService.create(request("BTC-USD", "60000", "0.15"));
        Trade second = tradeService.create(request("ETH-USD", "3000", "1"));

        assertThat(first.getId()).isEqualTo(1L);
        assertThat(second.getId()).isEqualTo(2L);
    }

    @Test
    void findRecentReturnsNewestFirst() {
        tradeService.create(request("BTC-USD", "1", "1"));
        tradeService.create(request("BTC-USD", "2", "1"));

        List<Trade> recent = tradeService.findRecent(null, 50);

        assertThat(recent).extracting(Trade::getId).containsExactly(2L, 1L);
    }

    @Test
    void createNormalizesSymbolToUpperCase() {
        Trade trade = tradeService.create(request("btc-usd", "60000", "0.15"));

        assertThat(trade.getSymbol()).isEqualTo("BTC-USD");
    }

    @Test
    void findRecentFiltersByNormalizedSymbolAndAppliesLimit() {
        tradeService.create(request("BTC-USD", "1", "1"));
        tradeService.create(request("ETH-USD", "2", "1"));
        tradeService.create(request("BTC-USD", "3", "1"));

        List<Trade> recent = tradeService.findRecent("btc-usd", 1);

        assertThat(recent).hasSize(1);
        assertThat(recent.get(0).getSymbol()).isEqualTo("BTC-USD");
        assertThat(recent.get(0).getId()).isEqualTo(3L);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        assertThatThrownBy(() -> tradeService.findById(99L))
                .isInstanceOf(TradeNotFoundException.class)
                .hasMessage("Trade not found with id: 99");
    }

    private CreateTradeRequest request(String symbol, String price, String quantity) {
        CreateTradeRequest req = new CreateTradeRequest();
        req.setSymbol(symbol);
        req.setPrice(new BigDecimal(price));
        req.setQuantity(new BigDecimal(quantity));
        return req;
    }
}
