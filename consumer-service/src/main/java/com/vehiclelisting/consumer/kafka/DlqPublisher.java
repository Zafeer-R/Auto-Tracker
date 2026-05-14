package com.vehiclelisting.consumer.kafka;

import com.vehiclelisting.consumer.config.VehicleListingKafkaProperties;
import com.vehiclelisting.consumer.model.DlqMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class DlqPublisher {

    private static final Logger log = LoggerFactory.getLogger(DlqPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final VehicleListingKafkaProperties kafkaProperties;
    private final Clock clock;

    public DlqPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            VehicleListingKafkaProperties kafkaProperties,
            Clock clock
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
        this.clock = clock;
    }

    public void publish(String originalPayload, String errorReason) {
        DlqMessage message = new DlqMessage(
                originalPayload,
                errorReason,
                kafkaProperties.listingTopic(),
                Instant.now(clock).toString()
        );

        kafkaTemplate.send(kafkaProperties.dlqTopic(), message);
        log.info("Sent event to DLQ topic={} reason={}", kafkaProperties.dlqTopic(), errorReason);
    }
}
