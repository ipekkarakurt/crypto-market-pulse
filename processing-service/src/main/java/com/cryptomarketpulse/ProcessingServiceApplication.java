package com.cryptomarketpulse;

import com.cryptomarketpulse.config.KafkaTopicProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class ProcessingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcessingServiceApplication.class, args);
    }
}
