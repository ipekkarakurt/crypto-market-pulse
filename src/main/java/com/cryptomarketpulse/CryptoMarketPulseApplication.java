package com.cryptomarketpulse;

import com.cryptomarketpulse.config.CoinbaseWebSocketProperties;
import com.cryptomarketpulse.config.KafkaTopicProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({CoinbaseWebSocketProperties.class, KafkaTopicProperties.class})
public class CryptoMarketPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoMarketPulseApplication.class, args);
    }
}
