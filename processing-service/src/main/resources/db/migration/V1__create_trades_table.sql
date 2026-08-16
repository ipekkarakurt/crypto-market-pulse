CREATE TABLE IF NOT EXISTS trades (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(16) NOT NULL,
    price NUMERIC(19, 8) NOT NULL,
    quantity NUMERIC(19, 8) NOT NULL,
    trade_time TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_trades_symbol_trade_time_desc
    ON trades(symbol, trade_time DESC);
