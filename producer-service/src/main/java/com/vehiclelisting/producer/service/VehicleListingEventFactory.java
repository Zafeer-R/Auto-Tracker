package com.vehiclelisting.producer.service;

import com.vehiclelisting.producer.dto.CreateListingRequest;
import com.vehiclelisting.producer.dto.PriceUpdateRequest;
import com.vehiclelisting.producer.dto.SoldListingRequest;
import com.vehiclelisting.producer.model.ListingStatus;
import com.vehiclelisting.producer.model.VehicleListingEvent;
import com.vehiclelisting.producer.model.VehicleListingEventType;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class VehicleListingEventFactory {

    private final Clock clock;

    public VehicleListingEventFactory() {
        this(Clock.systemUTC());
    }

    VehicleListingEventFactory(Clock clock) {
        this.clock = clock;
    }

    public VehicleListingEvent fromCreateRequest(CreateListingRequest request) {
        return new VehicleListingEvent(
                newEventId(),
                VehicleListingEventType.LISTING_CREATED.name(),
                request.listingId(),
                request.vin(),
                request.make(),
                request.model(),
                request.year(),
                request.price(),
                null,
                ListingStatus.ACTIVE.name(),
                request.dealerId(),
                now()
        );
    }

    public VehicleListingEvent fromPriceUpdateRequest(PriceUpdateRequest request) {
        return new VehicleListingEvent(
                newEventId(),
                VehicleListingEventType.PRICE_UPDATED.name(),
                request.listingId(),
                request.vin(),
                request.make(),
                request.model(),
                request.year(),
                request.price(),
                request.previousPrice(),
                ListingStatus.UPDATED.name(),
                request.dealerId(),
                now()
        );
    }

    public VehicleListingEvent fromSoldRequest(SoldListingRequest request) {
        return new VehicleListingEvent(
                newEventId(),
                VehicleListingEventType.LISTING_SOLD.name(),
                request.listingId(),
                request.vin(),
                request.make(),
                request.model(),
                request.year(),
                request.price(),
                null,
                ListingStatus.SOLD.name(),
                request.dealerId(),
                now()
        );
    }

    private String newEventId() {
        return UUID.randomUUID().toString();
    }

    private String now() {
        return Instant.now(clock).toString();
    }
}
