package com.cryptomarketpulse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cryptomarketpulse.exception.TradeNotFoundException;
import com.cryptomarketpulse.model.Trade;
import com.cryptomarketpulse.repository.TradeRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @InjectMocks
    private TradeService tradeService;

    @Test
    void ingestTradeNormalizesSymbolAndPersists() {
        Instant tradeTime = Instant.parse("2026-08-16T16:05:00Z");
        when(tradeRepository.save(any())).thenAnswer(invocation -> {
            Trade saved = invocation.getArgument(0, Trade.class);
            return new Trade(1L, saved.getSymbol(), saved.getPrice(), saved.getQuantity(), saved.getTradeTime());
        });

        Trade created = tradeService.ingestTrade(
                "btc-usd",
                new BigDecimal("60000"),
                new BigDecimal("0.15"),
                tradeTime);

        ArgumentCaptor<Trade> tradeCaptor = ArgumentCaptor.forClass(Trade.class);
        verify(tradeRepository).save(tradeCaptor.capture());
        assertThat(tradeCaptor.getValue().getSymbol()).isEqualTo("BTC-USD");
        assertThat(tradeCaptor.getValue().getTradeTime()).isEqualTo(tradeTime);
        assertThat(created.getId()).isEqualTo(1L);
    }

    @Test
    void findRecentNormalizesSymbolAndUsesLimit() {
        Trade trade = new Trade(3L, "BTC-USD", new BigDecimal("10"), new BigDecimal("1"), Instant.now());
        when(tradeRepository.findBySymbolOrderByTradeTimeDesc("BTC-USD", PageRequest.of(0, 100)))
                .thenReturn(List.of(trade));

        List<Trade> recent = tradeService.findRecent("btc-usd", 100);

        assertThat(recent).hasSize(1);
        assertThat(recent.get(0).getSymbol()).isEqualTo("BTC-USD");
        verify(tradeRepository).findBySymbolOrderByTradeTimeDesc(eq("BTC-USD"), eq(PageRequest.of(0, 100)));
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(tradeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tradeService.findById(99L))
                .isInstanceOf(TradeNotFoundException.class)
                .hasMessage("Trade not found with id: 99");
    }

}
