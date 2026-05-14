package com.vehiclelisting.producer.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VehicleListingKafkaProperties.class)
public class ProducerConfiguration {
}
