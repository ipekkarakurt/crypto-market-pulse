# Crypto Market Pulse

Kademe 3: Spring Boot + PostgreSQL + Coinbase WebSocket candle processing.

Bu aşamada uygulama `BTC-USD` için Coinbase'ten gerçek trade stream'ini dinler ve gelen trade'lerden 1 dakikalık candle üretip PostgreSQL'e yazar.

## Requirements

- Java 17+
- Maven 3.9+
- PostgreSQL 14+

## PostgreSQL setup (quick)

```bash
docker run --name crypto-pg \
  -e POSTGRES_DB=crypto_market_pulse \
  -e POSTGRES_USER=crypto_user \
  -e POSTGRES_PASSWORD=crypto_password \
  -p 5432:5432 -d postgres:16
```

## Run

```bash
mvn spring-boot:run
```

Varsayılan DB bağlantısı:

- URL: `jdbc:postgresql://localhost:5432/crypto_market_pulse`
- username: `crypto_user`
- password: `crypto_password`

İstersen environment variable ile override edebilirsin:

```bash
DB_URL=jdbc:postgresql://localhost:5432/crypto_market_pulse \
DB_USERNAME=crypto_user \
DB_PASSWORD=crypto_password \
mvn spring-boot:run
```

## Migration (Flyway)

`src/main/resources/db/migration/V1__create_trades_table.sql`

- `trades` tablosunu oluşturur
- `idx_trades_symbol_trade_time_desc` index'ini ekler

Flyway startup sırasında migration'ları otomatik çalıştırır.

## Entity

`Trade`

- `id`
- `symbol`
- `price`
- `quantity`
- `tradeTime`

## API

### 1 minute candles (Kademe 3)

`GET /markets/BTC-USD/candles?interval=1m`

Opsiyonel limit:

`GET /markets/BTC-USD/candles?interval=1m&limit=100`

Response örneği:

```json
[
  {
    "symbol": "BTC-USD",
    "start": "2026-08-16T18:30:00Z",
    "open": 60010,
    "high": 60120,
    "low": 59980,
    "close": 60090,
    "volume": 12.53,
    "tradeCount": 714
  }
]
```

## Coinbase WebSocket ingestion

Uygulama startup'ta Coinbase WebSocket'e subscribe olur:

- URL: `wss://ws-feed.exchange.coinbase.com`
- channel: `matches`
- product: `BTC-USD`
- reconnect delay: `5s`

Config:

```properties
coinbase.ws.enabled=true
coinbase.ws.url=wss://ws-feed.exchange.coinbase.com
coinbase.ws.symbol=BTC-USD
coinbase.ws.reconnect-delay=5s
```

WebSocket'i geçici kapatmak için:

```bash
COINBASE_WS_ENABLED=false mvn spring-boot:run
```

## SQL öğrenme quick check

```sql
SELECT symbol, start_time, open, high, low, close, volume, trade_count
FROM candles
WHERE symbol = 'BTC-USD'
ORDER BY start_time DESC
LIMIT 20;
```

Query plan görmek için:

```sql
EXPLAIN ANALYZE
SELECT symbol, start_time, open, high, low, close, volume, trade_count
FROM candles
WHERE symbol = 'BTC-USD'
ORDER BY start_time DESC
LIMIT 20;
```

## Transaction notları

- `TradeService#ingestTrade` -> `@Transactional`
- `CandleService#findRecent` -> `@Transactional(readOnly = true)`
- `Candle` entity'sindeki `@Version` alanı optimistic locking guard'ı sağlar.
- `CandleService#aggregateTrade` optimistic lock çatışmalarında kısa backoff ile otomatik retry uygular (max 3 deneme).

## Tests

```bash
mvn test
```

## Bitirme kriteri kontrol listesi

- [ ] App restart sonrası veri kaybolmuyor
- [ ] `GET /markets/BTC-USD/candles?interval=1m` doğru candle döndürüyor
- [ ] Uygulama 30-60 dakika kesintisiz trade topluyor
- [ ] `candles` tablosu ve index'i SQL ile doğrulandı
