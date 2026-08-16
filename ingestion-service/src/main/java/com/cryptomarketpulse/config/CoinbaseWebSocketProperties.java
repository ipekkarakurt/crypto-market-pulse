package com.cryptomarketpulse.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "coinbase.ws")
public class CoinbaseWebSocketProperties {

    private boolean enabled = true;
    private String url = "wss://ws-feed.exchange.coinbase.com";
    private String symbol = "BTC-USD";
    private Duration reconnectDelay = Duration.ofSeconds(5);
    private Duration maxReconnectDelay = Duration.ofSeconds(60);
    private double reconnectJitterFactor = 0.20d;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Duration getReconnectDelay() {
        return reconnectDelay;
    }

    public void setReconnectDelay(Duration reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }

    public Duration getMaxReconnectDelay() {
        return maxReconnectDelay;
    }

    public void setMaxReconnectDelay(Duration maxReconnectDelay) {
        this.maxReconnectDelay = maxReconnectDelay;
    }

    public double getReconnectJitterFactor() {
        return reconnectJitterFactor;
    }

    public void setReconnectJitterFactor(double reconnectJitterFactor) {
        this.reconnectJitterFactor = reconnectJitterFactor;
    }
}
