package com.cryptomarketpulse.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cryptomarketpulse.model.Candle;
import com.cryptomarketpulse.service.CandleService;
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
    private CandleService candleService;

    @Test
    void rejectsUnsupportedSymbol() throws Exception {
        mockMvc.perform(get("/markets/ETH-USD/candles").param("interval", "1m"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.symbol").value("only BTC-USD is supported at this stage"));
    }

    @Test
    void returnsCandlesForOneMinuteInterval() throws Exception {
        when(candleService.findRecent("BTC-USD", 100)).thenReturn(List.of(sampleCandle()));

        mockMvc.perform(get("/markets/BTC-USD/candles").param("interval", "1m"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("BTC-USD"))
                .andExpect(jsonPath("$[0].start").value("2026-08-16T16:05:00Z"))
                .andExpect(jsonPath("$[0].open").value(60000))
                .andExpect(jsonPath("$[0].high").value(60120))
                .andExpect(jsonPath("$[0].low").value(59980))
                .andExpect(jsonPath("$[0].close").value(60090))
                .andExpect(jsonPath("$[0].volume").value(12.53))
                .andExpect(jsonPath("$[0].tradeCount").value(714));
    }

    @Test
    void rejectsUnsupportedInterval() throws Exception {
        mockMvc.perform(get("/markets/BTC-USD/candles").param("interval", "5m"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.interval").value("only 1m interval is supported"));
    }

    private Candle sampleCandle() {
        return new Candle(
                "BTC-USD",
                Instant.parse("2026-08-16T16:05:00Z"),
                new BigDecimal("60000"),
                new BigDecimal("60120"),
                new BigDecimal("59980"),
                new BigDecimal("60090"),
                new BigDecimal("12.53"),
                714);
    }
}
