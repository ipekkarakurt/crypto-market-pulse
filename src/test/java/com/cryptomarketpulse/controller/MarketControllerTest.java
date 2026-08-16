package com.cryptomarketpulse.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cryptomarketpulse.model.Trade;
import com.cryptomarketpulse.service.TradeService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MarketController.class)
class MarketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeService tradeService;

    @Test
    void returnsBtcTradesWithDefaultLimit() throws Exception {
        when(tradeService.findRecent("BTC-USD", 100)).thenReturn(List.of(sampleTrade()));

        mockMvc.perform(get("/markets/BTC-USD/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("BTC-USD"))
                .andExpect(jsonPath("$[0].tradeTime").value("2026-08-16T16:05:00Z"));
    }

    @Test
    void rejectsUnsupportedSymbol() throws Exception {
        mockMvc.perform(get("/markets/ETH-USD/trades"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.symbol").value("only BTC-USD is supported at this stage"));
    }

    private Trade sampleTrade() {
        return new Trade(
                1L,
                "BTC-USD",
                new BigDecimal("60000"),
                new BigDecimal("0.15"),
                Instant.parse("2026-08-16T16:05:00Z"));
    }
}
