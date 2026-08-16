package com.cryptomarketpulse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cryptomarketpulse.model.Candle;
import com.cryptomarketpulse.model.Trade;
import com.cryptomarketpulse.repository.CandleRepository;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

@ExtendWith(MockitoExtension.class)
class CandleServiceTest {

    @Mock
    private CandleRepository candleRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private CandleService candleService;

    @Test
    @SuppressWarnings("null")
    void createsNewCandle() throws Exception {
        Instant tradeTime = Instant.parse("2026-08-16T18:30:12Z");
        Instant windowStart = Instant.parse("2026-08-16T18:30:00Z");
        mockTxManager();

        when(candleRepository.findBySymbolAndStartTime("BTC-USD", windowStart)).thenReturn(Optional.empty());
        when(candleRepository.saveAndFlush(any(Candle.class))).thenAnswer(invocation -> {
            Candle candle = Objects.requireNonNull(invocation.getArgument(0, Candle.class));
            setId(candle, 1L);
            return candle;
        });

        Candle saved = candleService.aggregateTrade(new Trade(
                "BTC-USD",
                new BigDecimal("60010"),
                new BigDecimal("0.20"),
                tradeTime));

        assertThat(saved.getOpen()).isEqualByComparingTo("60010");
        assertThat(saved.getHigh()).isEqualByComparingTo("60010");
        assertThat(saved.getLow()).isEqualByComparingTo("60010");
        assertThat(saved.getClose()).isEqualByComparingTo("60010");
        assertThat(saved.getVolume()).isEqualByComparingTo("0.20");
        assertThat(saved.getTradeCount()).isEqualTo(1);
    }

    @Test
    @SuppressWarnings("null")
    void retriesOnOptimisticLockAndThenUpdates() throws Exception {
        Instant tradeTime = Instant.parse("2026-08-16T18:30:18Z");
        Instant windowStart = Instant.parse("2026-08-16T18:30:00Z");
        mockTxManager();
        AtomicInteger fetchCount = new AtomicInteger();

        when(candleRepository.findBySymbolAndStartTime("BTC-USD", windowStart)).thenAnswer(invocation -> {
            Candle existing = new Candle(
                    "BTC-USD",
                    windowStart,
                    new BigDecimal("60010"),
                    new BigDecimal("60020"),
                    new BigDecimal("60000"),
                    new BigDecimal("60015"),
                    new BigDecimal("0.50"),
                    4);
            setId(existing, 1L);
            fetchCount.incrementAndGet();
            return Optional.of(existing);
        });
        when(candleRepository.saveAndFlush(any(Candle.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Candle.class, 1L))
                .thenAnswer(invocation -> invocation.getArgument(0, Candle.class));

        Candle saved = candleService.aggregateTrade(new Trade(
                "BTC-USD",
                new BigDecimal("60030"),
                new BigDecimal("0.10"),
                tradeTime));

        assertThat(fetchCount.get()).isEqualTo(2);
        assertThat(saved.getOpen()).isEqualByComparingTo("60010");
        assertThat(saved.getHigh()).isEqualByComparingTo("60030");
        assertThat(saved.getLow()).isEqualByComparingTo("60000");
        assertThat(saved.getClose()).isEqualByComparingTo("60030");
        assertThat(saved.getVolume()).isEqualByComparingTo("0.60");
        assertThat(saved.getTradeCount()).isEqualTo(5);
    }

    @Test
    @SuppressWarnings("null")
    void failsAfterMaxOptimisticRetries() throws Exception {
        Instant tradeTime = Instant.parse("2026-08-16T18:31:10Z");
        Instant windowStart = Instant.parse("2026-08-16T18:31:00Z");
        mockTxManager();

        when(candleRepository.findBySymbolAndStartTime("BTC-USD", windowStart)).thenReturn(Optional.empty());
        when(candleRepository.saveAndFlush(any(Candle.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Candle.class, 1L));

        assertThatThrownBy(() -> candleService.aggregateTrade(new Trade(
                        "BTC-USD",
                        new BigDecimal("60100"),
                        new BigDecimal("0.10"),
                        tradeTime)))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    private void mockTxManager() {
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(new SimpleTransactionStatus());
    }

    private void setId(Candle candle, Long id) throws Exception {
        Field idField = Candle.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(candle, id);
    }
}
