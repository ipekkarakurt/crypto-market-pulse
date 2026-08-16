package com.cryptomarketpulse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public class KafkaTopicProperties {

    private String marketTradesTopic = "market-trades";

    public String getMarketTradesTopic() {
        return marketTradesTopic;
    }

    public void setMarketTradesTopic(String marketTradesTopic) {
        this.marketTradesTopic = marketTradesTopic;
    }
}
