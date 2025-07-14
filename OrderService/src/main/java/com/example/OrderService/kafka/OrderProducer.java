
package com.example.OrderService.kafka;

import com.example.OrderService.message.model.OrderMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderMessage> kafkaTemplate;

    public void sendOrder(OrderMessage message) {
        kafkaTemplate.send("orders", message);
    }
}
