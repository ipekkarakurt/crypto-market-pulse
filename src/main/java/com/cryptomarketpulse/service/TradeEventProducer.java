package com.cryptomarketpulse.service;

import com.cryptomarketpulse.config.KafkaTopicProperties;
import com.cryptomarketpulse.dto.TradeEvent;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("ingestion")
public class TradeEventProducer {

    private final KafkaTemplate<String, TradeEvent> kafkaTemplate;
    private final KafkaTopicProperties topicProperties;

    public TradeEventProducer(KafkaTemplate<String, TradeEvent> kafkaTemplate, KafkaTopicProperties topicProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicProperties = topicProperties;
    }

    public void publish(TradeEvent event) {
        String key = event.symbol() + "|" + event.tradeTime().getEpochSecond() / 60L;
        kafkaTemplate.send(Objects.requireNonNull(topicProperties.getMarketTradesTopic()), key, event)
                .whenComplete((result, ex) -> {
            if (ex != null) {
                log.warn("Failed to publish trade event to Kafka: {}", ex.getMessage());
            }
        });
    }
}
