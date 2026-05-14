package com.vehiclelisting.consumer.mapper;

import com.vehiclelisting.consumer.entity.ListingHistory;
import com.vehiclelisting.consumer.model.VehicleListingEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ListingHistoryMapperTest {

    @Test
    void shouldMapEventToListingHistoryEntity() {
        ListingHistoryMapper mapper = new ListingHistoryMapper(
                Clock.fixed(Instant.parse("2026-04-30T15:00:00Z"), ZoneOffset.UTC)
        );
        VehicleListingEvent event = new VehicleListingEvent(
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

        ListingHistory entity = mapper.toEntity(event);

        assertThat(entity.getEventId()).isEqualTo("evt-1001-created");
        assertThat(entity.getEventType()).isEqualTo("LISTING_CREATED");
        assertThat(entity.getListingId()).isEqualTo("LST-1001");
        assertThat(entity.getVehicleYear()).isEqualTo(2022);
        assertThat(entity.getPrice()).isEqualByComparingTo("24500.00");
        assertThat(entity.getEventTimestamp()).isEqualTo(Instant.parse("2026-04-30T14:25:00Z"));
        assertThat(entity.getProcessedAt()).isEqualTo(Instant.parse("2026-04-30T15:00:00Z"));
    }
}
