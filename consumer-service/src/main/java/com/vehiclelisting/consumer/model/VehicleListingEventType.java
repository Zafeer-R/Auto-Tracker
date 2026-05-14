package com.vehiclelisting.consumer.model;

import java.util.Arrays;

public enum VehicleListingEventType {
    LISTING_CREATED,
    PRICE_UPDATED,
    LISTING_SOLD;

    public static boolean isSupported(String value) {
        return Arrays.stream(values()).anyMatch(type -> type.name().equals(value));
    }
}
