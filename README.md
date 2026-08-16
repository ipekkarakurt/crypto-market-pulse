# Crypto Market Pulse

Kademe 2: Spring Boot + PostgreSQL + Coinbase WebSocket ingestion.

Bu aşamada uygulama `BTC-USD` için Coinbase'ten gerçek trade stream'ini dinler ve gelen trade'leri PostgreSQL'e yazar.

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

### Live market trades endpoint (Kademe 2)

`GET /markets/BTC-USD/trades`

Opsiyonel limit:

`GET /markets/BTC-USD/trades?limit=100`

Not: Kademe 2'de sadece `BTC-USD` desteklenir.

Response örneği:

```json
[
  {
    "id": 248829,
    "symbol": "BTC-USD",
    "price": 63210.11,
    "quantity": 0.0021,
    "tradeTime": "2026-08-16T18:05:00.123Z"
  }
]
```

- `limit` default: `100`
- allowed range: `1..1000`
- newest first (`tradeTime DESC`)

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
SELECT id, symbol, price, quantity, trade_time
FROM trades
WHERE symbol = 'BTC-USD'
ORDER BY trade_time DESC
LIMIT 20;
```

Query plan görmek için:

```sql
EXPLAIN ANALYZE
SELECT id, symbol, price, quantity, trade_time
FROM trades
WHERE symbol = 'BTC-USD'
ORDER BY trade_time DESC
LIMIT 20;
```

## Transaction notları

- `TradeService#ingestTrade` -> `@Transactional`
- `TradeService#findRecent` / `findById` -> `@Transactional(readOnly = true)`

## Tests

```bash
mvn test
```

## Bitirme kriteri kontrol listesi

- [ ] App restart sonrası veri kaybolmuyor
- [ ] `GET /markets/BTC-USD/trades` gerçek veriyi döndürüyor
- [ ] `GET /markets/BTC-USD/trades?limit=100` çalışıyor
- [ ] Uygulama 30-60 dakika kesintisiz trade topluyor
- [ ] `trades` tablosu ve index'i SQL ile doğrulandı
