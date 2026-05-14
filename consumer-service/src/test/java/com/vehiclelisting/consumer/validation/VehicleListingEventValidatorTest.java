package com.vehiclelisting.consumer.validation;

import com.vehiclelisting.consumer.exception.EventValidationException;
import com.vehiclelisting.consumer.model.VehicleListingEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehicleListingEventValidatorTest {

    private final VehicleListingEventValidator validator = new VehicleListingEventValidator();

    @Test
    void shouldAllowValidListingCreatedEvent() {
        assertThatCode(() -> validator.validate(validCreatedEvent())).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectMissingListingId() {
        VehicleListingEvent event = new VehicleListingEvent(
                "evt-1", "LISTING_CREATED", null, "VIN", "Toyota", "Camry",
                2022, new BigDecimal("24500.00"), null, "ACTIVE", "DLR-1", "2026-04-30T14:25:00Z"
        );

        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(EventValidationException.class)
                .hasMessageContaining("listingId");
    }

    @Test
    void shouldRejectUnsupportedEventType() {
        VehicleListingEvent event = new VehicleListingEvent(
                "evt-1", "UNKNOWN", "LST-1", "VIN", "Toyota", "Camry",
                2022, new BigDecimal("24500.00"), null, "ACTIVE", "DLR-1", "2026-04-30T14:25:00Z"
        );

        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(EventValidationException.class)
                .hasMessageContaining("Unsupported eventType");
    }

    @Test
    void shouldRejectNegativePrice() {
        VehicleListingEvent event = new VehicleListingEvent(
                "evt-1", "PRICE_UPDATED", "LST-1", "VIN", "Toyota", "Camry",
                2022, new BigDecimal("-1.00"), new BigDecimal("24500.00"), "UPDATED", "DLR-1", "2026-04-30T14:25:00Z"
        );

        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(EventValidationException.class)
                .hasMessageContaining("price");
    }

    @Test
    void shouldRejectInvalidTimestamp() {
        VehicleListingEvent event = new VehicleListingEvent(
                "evt-1", "LISTING_CREATED", "LST-1", "VIN", "Toyota", "Camry",
                2022, new BigDecimal("24500.00"), null, "ACTIVE", "DLR-1", "not-a-date"
        );

        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(EventValidationException.class)
                .hasMessageContaining("eventTimestamp");
    }

    @Test
    void shouldRejectSoldEventWithoutSoldStatus() {
        VehicleListingEvent event = new VehicleListingEvent(
                "evt-1", "LISTING_SOLD", "LST-1", "VIN", "Toyota", "Camry",
                2022, new BigDecimal("24500.00"), null, "ACTIVE", "DLR-1", "2026-04-30T14:25:00Z"
        );

        assertThatThrownBy(() -> validator.validate(event))
                .isInstanceOf(EventValidationException.class)
                .hasMessageContaining("SOLD");
    }

    private VehicleListingEvent validCreatedEvent() {
        return new VehicleListingEvent(
                "evt-1001-created",
                "LISTING_CREATED",
                "LST-1001",
                "1HGCM82633A004352",
                "Toyota",
                "Camry",
                2022,
                new BigDecimal("24500.00"),
                null,
                "ACTIVE",
                "DLR-2001",
                "2026-04-30T14:25:00Z"
        );
    }
}
