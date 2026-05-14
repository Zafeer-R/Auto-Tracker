package com.vehiclelisting.producer.dto;

public record EventPublishResponse(
        String status,
        String topic,
        String eventType,
        String listingId,
        String message
) {

    public static EventPublishResponse accepted(String topic, String eventType, String listingId) {
        return new EventPublishResponse(
                "ACCEPTED",
                topic,
                eventType,
                listingId,
                "Vehicle listing event accepted for publishing"
        );
    }
}
