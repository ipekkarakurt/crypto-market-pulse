package com.cryptomarketpulse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.cryptomarketpulse.model.Candle;
import com.cryptomarketpulse.model.Trade;
import com.cryptomarketpulse.repository.CandleRepository;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CandleServiceTest {

    @Mock
    private CandleRepository candleRepository;

    @InjectMocks
    private CandleService candleService;

    private final ExecutorService pool = Executors.newFixedThreadPool(8);

    @AfterEach
    void tearDown() {
        pool.shutdownNow();
    }

    @Test
    @SuppressWarnings("null")
    void aggregatesConcurrentTradesInSameOneMinuteWindow() throws Exception {
        Instant minute = Instant.parse("2026-08-16T18:30:12Z");
        AtomicReference<Candle> store = new AtomicReference<>();

        when(candleRepository.findBySymbolAndStartTime(eq("BTC-USD"), eq(minute.truncatedTo(java.time.temporal.ChronoUnit.MINUTES))))
                .thenAnswer(invocation -> Optional.ofNullable(store.get()));

        when(candleRepository.save(any(Candle.class))).thenAnswer(invocation -> {
            Candle candle = Objects.requireNonNull(invocation.getArgument(0, Candle.class));
            if (candle.getId() == null) {
                setId(candle, 1L);
            }
            store.set(candle);
            return candle;
        });

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            final int idx = i;
            tasks.add(() -> {
                BigDecimal price = new BigDecimal(60000 + idx);
                candleService.aggregateTrade(new Trade("BTC-USD", price, new BigDecimal("0.10"), minute.plusSeconds(idx % 20)));
                return null;
            });
        }

        List<Future<Void>> futures = pool.invokeAll(tasks);
        for (Future<Void> future : futures) {
            future.get();
        }

        Candle candle = store.get();
        assertThat(candle).isNotNull();
        assertThat(candle.getTradeCount()).isEqualTo(50);
        assertThat(candle.getOpen()).isBetween(new BigDecimal("60000"), new BigDecimal("60049"));
        assertThat(candle.getHigh()).isEqualByComparingTo("60049");
        assertThat(candle.getLow()).isEqualByComparingTo("60000");
        assertThat(candle.getVolume()).isEqualByComparingTo("5.00");
    }

    private void setId(Candle candle, Long id) throws Exception {
        Field idField = Candle.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(candle, id);
    }
}
