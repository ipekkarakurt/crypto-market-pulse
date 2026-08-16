# Crypto Market Pulse

Real-time crypto market data backend. Stage 0: in-memory trade REST API.

## Requirements

- Java 17+
- Maven 3.9+

## Run

```bash
mvn spring-boot:run
```

Server starts on `http://localhost:8080`.

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

Allowed symbols: `BTC-USD`, `ETH-USD`, `SOL-USD` (case-insensitive; stored uppercase).

Response `201 Created` with `Location: /trades/{id}`:

```json
{
  "id": 1,
  "symbol": "BTC-USD",
  "price": 60000,
  "quantity": 0.15,
  "timestamp": "2026-08-16T16:05:00.123Z"
}
```

### List recent trades

`GET /trades`

Query parameters:

| Param | Required | Default | Notes |
|-------|----------|---------|-------|
| `symbol` | no | all symbols | e.g. `BTC-USD` |
| `limit` | no | `50` | min `1`, max `1000` |

Example: `GET /trades?symbol=BTC-USD&limit=50`

Results are newest-first (highest id first).

### Get trade by id

`GET /trades/{id}`

Returns `404` when the trade does not exist.

### Error response

```json
{
  "timestamp": "2026-08-16T16:05:00.123Z",
  "status": 400,
  "error": "Validation failed",
  "fields": {
    "symbol": "must be BTC-USD, ETH-USD, or SOL-USD"
  }
}
```

`fields` is omitted when the error is not field-level (e.g. 404, malformed JSON).

## Postman quick test

1. `POST http://localhost:8080/trades` with JSON body above — expect `201` and `Location`
2. `GET http://localhost:8080/trades`
3. `GET http://localhost:8080/trades?symbol=BTC-USD&limit=10`
4. `GET http://localhost:8080/trades/1`

## Tests

```bash
mvn test
```

## Project layout

```
src/main/java/com/cryptomarketpulse/
├── CryptoMarketPulseApplication.java
├── controller/TradeController.java
├── service/TradeService.java
├── repository/
│   ├── TradeRepository.java
│   └── InMemoryTradeRepository.java
├── model/Trade.java
├── dto/
│   ├── CreateTradeRequest.java
│   └── TradeResponse.java
└── exception/
    ├── ErrorResponse.java
    ├── TradeNotFoundException.java
    └── GlobalExceptionHandler.java
```
