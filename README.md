# Crypto Market Pulse

Kademe 5.5: Kafka ile ayrılmış servisleri Maven modüllerine bölme.

Bu aşamada akış:

`Coinbase -> ingestion-service -> Kafka(topic: market-trades) -> processing-service -> PostgreSQL`

## Requirements

- Docker Desktop (veya Docker Engine + Compose plugin)

## Quick start (tek komut)

```bash
docker compose up
```

Servisler:

- Processing API: `http://localhost:8080`
- PostgreSQL: `localhost:5432`
- Kafka broker: `localhost:9092`

İlk çalıştırmada her servis kendi Dockerfile'ı ile build edilir:

- `ingestion-service/Dockerfile`
- `processing-service/Dockerfile`

Durdurma:

```bash
docker compose down
```

Veriyi de silmek istersen:

```bash
docker compose down -v
```

## Kafka + servis rolleri

- `ingestion-service` (ayrı Maven modülü)
  - Coinbase WebSocket dinler
  - trade event'lerini Kafka `market-trades` topic'ine üretir (producer)
  - DB bağlantısı yok
- `processing-service` (ayrı Maven modülü)
  - Kafka `market-trades` topic'inden event tüketir (consumer)
  - event'i candle'a dönüştürüp PostgreSQL'e yazar
  - `GET /markets/BTC-USD/candles?interval=1m` endpoint'ini sunar

## Kafka kavramlarını projede nerede görüyoruz

- `producer`: `TradeEventProducer`
- `consumer`: `TradeEventConsumer`
- `topic`: `market-trades`
- `partition`: consumer loglarında `partition` değeri
- `offset`: consumer loglarında `offset` değeri
- `consumer group`: `market-processing-service`
- `serialization`: producer `JsonSerializer`, consumer `JsonDeserializer`
- `async communication`: ingestion ve processing servisleri birbirinden bağımsız çalışır

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

`processing-service/src/main/resources/db/migration/V1__create_trades_table.sql`

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

Docker Compose içindeki servisler bu environment variable'ları kullanır:

- `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`
- `APP_KAFKA_MARKET_TRADES_TOPIC=market-trades`
- processing için: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`

## Multi-module yapı

- `shared-model`: servisler arası paylaşılan DTO/event modelleri (`TradeEvent`)
- `ingestion-service`: Coinbase WebSocket -> Kafka producer
- `processing-service`: Kafka consumer -> Candle aggregation -> PostgreSQL + REST API

## Docker yapısı (öğrenme notu)

- `ingestion-service/Dockerfile` ve `processing-service/Dockerfile`: ayrı app image'ları üretir (multi-stage build).
- `image`: app servisleri için `crypto-market-pulse-ingestion:latest` ve `crypto-market-pulse-processing:latest`; DB için `postgres:16`; Kafka için `confluentinc/cp-kafka:7.6.1`.
- `container`: `crypto-market-pulse-ingestion`, `crypto-market-pulse-processing`, `crypto-market-pulse-postgres`, `crypto-market-pulse-kafka`.
- `volume`: `pgdata` ile PostgreSQL verisi kalıcı tutulur.
- `environment variables`: Kafka, DB ve websocket ayarları compose içinden verilir.
- `network`: `crypto-net` bridge ağı ile container'lar birbirini `postgres` hostname'i ile görür.
- `port mapping`: processing `8080:8080`, postgres `5432:5432`, kafka `9092:9092`.

## Dayanıklılık deneyi (zorunlu)

1. Sistemi başlat:
   - `docker compose up`
2. Processing servisini kapat:
   - `docker compose stop processing-service`
3. Bir süre bekle (ingestion Kafka'ya yazmaya devam eder).
4. Processing servisini tekrar aç:
   - `docker compose start processing-service`
5. Gözlemle:
   - processing loglarında birikmiş eventlerin offset bazında işlendiğini gör
   - candles endpoint'inde verinin güncellendiğini doğrula

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

- `Candle` entity'sindeki `@Version` alanı optimistic locking guard'ı sağlar.
- `CandleService#aggregateTrade` optimistic lock çatışmalarında kısa backoff ile otomatik retry uygular (max 3 deneme).

## Tests

```bash
mvn clean test
```

## Bitirme kriteri kontrol listesi

- [ ] App restart sonrası veri kaybolmuyor
- [ ] `GET /markets/BTC-USD/candles?interval=1m` doğru candle döndürüyor
- [ ] Processing durup açıldığında Kafka'daki birikmiş eventler işleniyor
- [ ] Producer/consumer/topic/partition/offset/consumer group kavramlarını kendi cümlelerinle anlatabiliyorsun
- [ ] `candles` tablosu ve index'i SQL ile doğrulandı
