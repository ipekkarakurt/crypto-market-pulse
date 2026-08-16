# Crypto Market Pulse

Kademe 1: Spring Boot + PostgreSQL + JPA/Hibernate + Flyway.

Bu aşamada trade verisi memory yerine PostgreSQL'de tutulur; uygulama restart olsa bile veri kaybolmaz.

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

### Create trade

`POST /trades`

```json
{
  "symbol": "BTC-USD",
  "price": 60000,
  "quantity": 0.15
}
```

Response (`201 Created`):

```json
{
  "id": 1,
  "symbol": "BTC-USD",
  "price": 60000,
  "quantity": 0.15,
  "tradeTime": "2026-08-16T16:05:00.123Z"
}
```

### List trades by symbol

`GET /trades?symbol=BTC-USD`

### List trades by symbol with limit

`GET /trades?symbol=BTC-USD&limit=100`

- `limit` default: `50`
- allowed range: `1..1000`
- newest first (`tradeTime DESC`)

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

- `TradeService#create` -> `@Transactional`
- `TradeService#findRecent` / `findById` -> `@Transactional(readOnly = true)`

## Tests

```bash
mvn test
```

## Bitirme kriteri kontrol listesi

- [ ] App restart sonrası veri kaybolmuyor
- [ ] `GET /trades?symbol=BTC-USD` çalışıyor
- [ ] `GET /trades?symbol=BTC-USD&limit=100` çalışıyor
- [ ] `trades` tablosu ve index'i SQL ile doğrulandı
