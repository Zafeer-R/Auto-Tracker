package com.vehiclelisting.consumer.mapper;

import com.vehiclelisting.consumer.entity.ListingHistory;
import com.vehiclelisting.consumer.model.VehicleListingEvent;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class ListingHistoryMapper {

    private final Clock clock;

    public ListingHistoryMapper() {
        this(Clock.systemUTC());
    }

    public ListingHistoryMapper(Clock clock) {
        this.clock = clock;
    }

    public ListingHistory toEntity(VehicleListingEvent event) {
        ListingHistory entity = new ListingHistory();
        entity.setEventId(event.eventId());
        entity.setEventType(event.eventType());
        entity.setListingId(event.listingId());
        entity.setVin(event.vin());
        entity.setMake(event.make());
        entity.setModel(event.model());
        entity.setVehicleYear(event.year());
        entity.setPrice(event.price());
        entity.setPreviousPrice(event.previousPrice());
        entity.setStatus(event.status());
        entity.setDealerId(event.dealerId());
        entity.setEventTimestamp(Instant.parse(event.eventTimestamp()));
        entity.setProcessedAt(Instant.now(clock));
        return entity;
    }
}
