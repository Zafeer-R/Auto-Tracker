package com.vehiclelisting.producer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vehicle-listing.kafka")
public record VehicleListingKafkaProperties(String listingTopic) {
}
