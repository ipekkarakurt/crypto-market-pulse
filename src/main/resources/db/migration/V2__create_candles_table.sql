CREATE TABLE IF NOT EXISTS candles (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(16) NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    open NUMERIC(19, 8) NOT NULL,
    high NUMERIC(19, 8) NOT NULL,
    low NUMERIC(19, 8) NOT NULL,
    close NUMERIC(19, 8) NOT NULL,
    volume NUMERIC(19, 8) NOT NULL,
    trade_count BIGINT NOT NULL,
    CONSTRAINT uq_candles_symbol_start UNIQUE (symbol, start_time)
);

CREATE INDEX IF NOT EXISTS idx_candles_symbol_start_desc
    ON candles(symbol, start_time DESC);
