package com.vehiclelisting.consumer.validation;

import com.vehiclelisting.consumer.exception.EventValidationException;
import com.vehiclelisting.consumer.model.VehicleListingEvent;
import com.vehiclelisting.consumer.model.VehicleListingEventType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class VehicleListingEventValidator {

    public void validate(VehicleListingEvent event) {
        if (event == null) {
            throw new EventValidationException("Event payload must not be null");
        }

        requireNotBlank(event.eventId(), "eventId");
        requireNotBlank(event.eventType(), "eventType");
        requireSupportedEventType(event.eventType());
        requireNotBlank(event.listingId(), "listingId");
        requireNotBlank(event.dealerId(), "dealerId");
        requireNotBlank(event.status(), "status");
        requireValidTimestamp(event.eventTimestamp());
        requireNonNegative(event.price(), "price");
        requireNonNegative(event.previousPrice(), "previousPrice");

        if ("LISTING_CREATED".equals(event.eventType())) {
            requireNotBlank(event.vin(), "vin");
            requireNotBlank(event.make(), "make");
            requireNotBlank(event.model(), "model");
            requireNotNull(event.year(), "year");
            requireNotNull(event.price(), "price");
        }

        if ("PRICE_UPDATED".equals(event.eventType())) {
            requireNotNull(event.price(), "price");
        }

        if ("LISTING_SOLD".equals(event.eventType()) && !"SOLD".equals(event.status())) {
            throw new EventValidationException("Invalid field: LISTING_SOLD must have status SOLD");
        }
    }

    private void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new EventValidationException("Missing required field: " + fieldName);
        }
    }

    private void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new EventValidationException("Missing required field: " + fieldName);
        }
    }

    private void requireSupportedEventType(String eventType) {
        if (!VehicleListingEventType.isSupported(eventType)) {
            throw new EventValidationException("Unsupported eventType: " + eventType);
        }
    }

    private void requireValidTimestamp(String timestamp) {
        requireNotBlank(timestamp, "eventTimestamp");
        try {
            Instant.parse(timestamp);
        } catch (Exception exception) {
            throw new EventValidationException("Invalid field: eventTimestamp must be ISO-8601 UTC");
        }
    }

    private void requireNonNegative(BigDecimal value, String fieldName) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new EventValidationException("Invalid field: " + fieldName + " must not be negative");
        }
    }
}
