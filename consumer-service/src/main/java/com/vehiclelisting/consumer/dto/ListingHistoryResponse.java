package com.vehiclelisting.consumer.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ListingHistoryResponse(
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
        Instant eventTimestamp,
        Instant processedAt
) {
}
