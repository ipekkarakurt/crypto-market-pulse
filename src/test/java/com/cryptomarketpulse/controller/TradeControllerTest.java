package com.cryptomarketpulse.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cryptomarketpulse.exception.TradeNotFoundException;
import com.cryptomarketpulse.model.Trade;
import com.cryptomarketpulse.service.TradeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TradeController.class)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TradeService tradeService;

    @Test
    void createReturnsCreatedWithLocation() throws Exception {
        when(tradeService.create(any())).thenReturn(sampleTrade(1L, "BTC-USD"));

        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "symbol", "BTC-USD",
                                "price", 60000,
                                "quantity", 0.15))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/trades/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.symbol").value("BTC-USD"))
                .andExpect(jsonPath("$.price").value(60000))
                .andExpect(jsonPath("$.quantity").value(0.15))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createRejectsInvalidSymbol() throws Exception {
        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "symbol", "DOGE-USD",
                                "price", 1,
                                "quantity", 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.symbol").value("must be BTC-USD, ETH-USD, or SOL-USD"));
    }

    @Test
    void createRejectsMissingPrice() throws Exception {
        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "symbol", "BTC-USD",
                                "quantity", 0.15))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.price").exists());
    }

    @Test
    void createRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed JSON request"));
    }

    @Test
    void findRecentReturnsTrades() throws Exception {
        when(tradeService.findRecent(null, 50)).thenReturn(List.of(sampleTrade(2L, "ETH-USD")));

        mockMvc.perform(get("/trades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].symbol").value("ETH-USD"));
    }

    @Test
    void findRecentRejectsInvalidLimit() throws Exception {
        mockMvc.perform(get("/trades").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.limit").exists());
    }

    @Test
    void findByIdReturnsTrade() throws Exception {
        when(tradeService.findById(1L)).thenReturn(sampleTrade(1L, "BTC-USD"));

        mockMvc.perform(get("/trades/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void findByIdReturnsNotFound() throws Exception {
        when(tradeService.findById(999L)).thenThrow(new TradeNotFoundException(999L));

        mockMvc.perform(get("/trades/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Trade not found with id: 999"));
    }

    @Test
    void findByIdRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/trades/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for parameter 'id'"));
    }

    private Trade sampleTrade(Long id, String symbol) {
        return new Trade(
                id,
                symbol,
                new BigDecimal("60000"),
                new BigDecimal("0.15"),
                Instant.parse("2026-08-16T16:05:00Z"));
    }
}
