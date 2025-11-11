package net.javaguides.kafka;

import org.slf4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import net.javaguides.base_domains.dto.OrderEvent;

@Service
public class OrderConsumer {
    
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OrderConsumer.class);

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeOrder(OrderEvent orderEvent) {
        LOGGER.info("Order event received in stock service: {}", orderEvent);

        LOGGER.info(String.format("Order Event received in stock service => %s", orderEvent.toString()));

        // save order event data to the database
    }
}
