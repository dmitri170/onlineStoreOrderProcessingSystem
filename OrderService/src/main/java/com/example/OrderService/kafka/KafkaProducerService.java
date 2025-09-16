package com.example.OrderService.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsConfig kafkaTopicsConfig; // инжектим конфиг

    public void sendMessage(String topic, String key, Object message) {
        log.info("Sending message to topic: {}, key: {}, message: {}", topic, key, message);

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message sent successfully to topic: {} with offset: {}",
                        topic, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send message to topic: {}, error: {}", topic, ex.getMessage());
            }
        });
    }

    public void sendOrderEvent(String eventType, Object orderData) {
        String topic = kafkaTopicsConfig.getOrderEvents();
        sendMessage(topic, eventType, orderData);
    }
}
