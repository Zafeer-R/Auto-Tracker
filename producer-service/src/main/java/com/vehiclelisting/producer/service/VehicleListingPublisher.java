package com.vehiclelisting.producer.service;

import com.vehiclelisting.producer.config.VehicleListingKafkaProperties;
import com.vehiclelisting.producer.dto.EventPublishResponse;
import com.vehiclelisting.producer.model.VehicleListingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class VehicleListingPublisher {

    private static final Logger log = LoggerFactory.getLogger(VehicleListingPublisher.class);

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final VehicleListingKafkaProperties kafkaProperties;

    public VehicleListingPublisher(
            KafkaTemplate<Object, Object> kafkaTemplate,
            VehicleListingKafkaProperties kafkaProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    public EventPublishResponse publish(VehicleListingEvent event) {
        String topic = kafkaProperties.listingTopic();
        CompletableFuture<SendResult<Object, Object>> future =
                kafkaTemplate.send(topic, event.listingId(), event);

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                log.error(
                        "Failed to publish vehicle listing event topic={} eventType={} eventId={} listingId={}",
                        topic,
                        event.eventType(),
                        event.eventId(),
                        event.listingId(),
                        exception
                );
                return;
            }

            log.info(
                    "Published vehicle listing event topic={} partition={} offset={} eventType={} eventId={} listingId={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    event.eventType(),
                    event.eventId(),
                    event.listingId()
            );
        });

        return EventPublishResponse.accepted(topic, event.eventType(), event.listingId());
    }
}
