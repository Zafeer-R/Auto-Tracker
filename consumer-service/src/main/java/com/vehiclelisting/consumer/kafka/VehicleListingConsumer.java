package com.vehiclelisting.consumer.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehiclelisting.consumer.entity.ListingHistory;
import com.vehiclelisting.consumer.exception.EventValidationException;
import com.vehiclelisting.consumer.mapper.ListingHistoryMapper;
import com.vehiclelisting.consumer.model.VehicleListingEvent;
import com.vehiclelisting.consumer.repository.ListingHistoryRepository;
import com.vehiclelisting.consumer.validation.VehicleListingEventValidator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class VehicleListingConsumer {

    private static final Logger log = LoggerFactory.getLogger(VehicleListingConsumer.class);

    private final ObjectMapper objectMapper;
    private final VehicleListingEventValidator validator;
    private final ListingHistoryMapper mapper;
    private final ListingHistoryRepository repository;
    private final DlqPublisher dlqPublisher;

    public VehicleListingConsumer(
            ObjectMapper objectMapper,
            VehicleListingEventValidator validator,
            ListingHistoryMapper mapper,
            ListingHistoryRepository repository,
            DlqPublisher dlqPublisher
    ) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.mapper = mapper;
        this.repository = repository;
        this.dlqPublisher = dlqPublisher;
    }

    @KafkaListener(topics = "${vehicle-listing.kafka.listing-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record) {
        log.info("Received listing event key={} partition={} offset={}", record.key(), record.partition(), record.offset());
        process(record.value());
    }

    public void process(String rawPayload) {
        try {
            VehicleListingEvent event = objectMapper.readValue(rawPayload, VehicleListingEvent.class);
            validator.validate(event);
            log.info("Validation passed for eventId={} listingId={} eventType={}",
                    event.eventId(), event.listingId(), event.eventType());

            ListingHistory listingHistory = mapper.toEntity(event);
            repository.save(listingHistory);
            log.info("Persisted listing event eventId={} listingId={}", event.eventId(), event.listingId());
        } catch (JsonProcessingException exception) {
            log.error("Failed to deserialize listing event payload={}", rawPayload, exception);
            sendToDlq(rawPayload, "Invalid JSON: " + exception.getOriginalMessage());
        } catch (EventValidationException exception) {
            log.error("Rejected invalid listing event reason={} payload={}", exception.getMessage(), rawPayload);
            sendToDlq(rawPayload, exception.getMessage());
        } catch (DataIntegrityViolationException exception) {
            log.warn("Skipped duplicate or invalid database record payload={}", rawPayload, exception);
            sendToDlq(rawPayload, "Database persistence failed: " + exception.getMostSpecificCause().getMessage());
        } catch (Exception exception) {
            log.error("Unexpected error while processing listing event payload={}", rawPayload, exception);
            sendToDlq(rawPayload, "Unexpected processing error: " + exception.getMessage());
        }
    }

    private void sendToDlq(String rawPayload, String reason) {
        try {
            dlqPublisher.publish(rawPayload, reason);
        } catch (Exception exception) {
            log.error("Failed to publish listing event to DLQ reason={} payload={}", reason, rawPayload, exception);
        }
    }
}
