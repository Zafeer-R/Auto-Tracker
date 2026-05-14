package com.vehiclelisting.consumer.model;

import java.math.BigDecimal;

public record VehicleListingEvent(
        String eventId,
        String eventType,
        String listingId,
        String vin,
        String make,
        String model,
        Integer year,
        BigDecimal price,
        BigDecimal previousPrice,
        String status,
        String dealerId,
        String eventTimestamp
) {
}
