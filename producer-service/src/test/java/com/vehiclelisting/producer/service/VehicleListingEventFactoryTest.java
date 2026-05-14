package com.vehiclelisting.producer.service;

import com.vehiclelisting.producer.dto.CreateListingRequest;
import com.vehiclelisting.producer.dto.PriceUpdateRequest;
import com.vehiclelisting.producer.dto.SoldListingRequest;
import com.vehiclelisting.producer.model.VehicleListingEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleListingEventFactoryTest {

    private static final Instant FIXED_TIME = Instant.parse("2026-04-30T14:25:00Z");

    private final VehicleListingEventFactory factory =
            new VehicleListingEventFactory(Clock.fixed(FIXED_TIME, ZoneOffset.UTC));

    @Test
    void shouldCreateListingCreatedEventWithActiveStatus() {
        CreateListingRequest request = new CreateListingRequest(
                "LST-1001",
                "1HGCM82633A004352",
                "Toyota",
                "Camry",
                2022,
                new BigDecimal("24500.00"),
                "DLR-2001"
        );

        VehicleListingEvent event = factory.fromCreateRequest(request);

        assertThat(event.eventId()).isNotBlank();
        assertThat(event.eventType()).isEqualTo("LISTING_CREATED");
        assertThat(event.status()).isEqualTo("ACTIVE");
        assertThat(event.listingId()).isEqualTo("LST-1001");
        assertThat(event.previousPrice()).isNull();
        assertThat(event.eventTimestamp()).isEqualTo("2026-04-30T14:25:00Z");
    }

    @Test
    void shouldCreatePriceUpdatedEventWithUpdatedStatus() {
        PriceUpdateRequest request = new PriceUpdateRequest(
                "LST-1001",
                "1HGCM82633A004352",
                "Toyota",
                "Camry",
                2022,
                new BigDecimal("24500.00"),
                new BigDecimal("23900.00"),
                "DLR-2001"
        );

        VehicleListingEvent event = factory.fromPriceUpdateRequest(request);

        assertThat(event.eventType()).isEqualTo("PRICE_UPDATED");
        assertThat(event.status()).isEqualTo("UPDATED");
        assertThat(event.previousPrice()).isEqualByComparingTo("24500.00");
        assertThat(event.price()).isEqualByComparingTo("23900.00");
        assertThat(event.eventTimestamp()).isEqualTo("2026-04-30T14:25:00Z");
    }

    @Test
    void shouldCreateListingSoldEventWithSoldStatus() {
        SoldListingRequest request = new SoldListingRequest(
                "LST-1001",
                "1HGCM82633A004352",
                "Toyota",
                "Camry",
                2022,
                new BigDecimal("23900.00"),
                "DLR-2001"
        );

        VehicleListingEvent event = factory.fromSoldRequest(request);

        assertThat(event.eventType()).isEqualTo("LISTING_SOLD");
        assertThat(event.status()).isEqualTo("SOLD");
        assertThat(event.listingId()).isEqualTo("LST-1001");
        assertThat(event.eventTimestamp()).isEqualTo("2026-04-30T14:25:00Z");
    }
}
