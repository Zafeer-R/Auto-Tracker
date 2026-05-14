package com.vehiclelisting.producer.controller;

import com.vehiclelisting.producer.dto.CreateListingRequest;
import com.vehiclelisting.producer.dto.EventPublishResponse;
import com.vehiclelisting.producer.dto.PriceUpdateRequest;
import com.vehiclelisting.producer.dto.SoldListingRequest;
import com.vehiclelisting.producer.model.VehicleListingEvent;
import com.vehiclelisting.producer.service.VehicleListingEventFactory;
import com.vehiclelisting.producer.service.VehicleListingPublisher;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/listings/events")
public class VehicleListingEventController {

    private static final Logger log = LoggerFactory.getLogger(VehicleListingEventController.class);

    private final VehicleListingEventFactory eventFactory;
    private final VehicleListingPublisher publisher;

    public VehicleListingEventController(VehicleListingEventFactory eventFactory, VehicleListingPublisher publisher) {
        this.eventFactory = eventFactory;
        this.publisher = publisher;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EventPublishResponse createListing(@Valid @RequestBody CreateListingRequest request) {
        log.info("Create listing request received for listingId={}", request.listingId());
        VehicleListingEvent event = eventFactory.fromCreateRequest(request);
        return publisher.publish(event);
    }

    @PostMapping("/price-update")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EventPublishResponse updatePrice(@Valid @RequestBody PriceUpdateRequest request) {
        log.info("Price update request received for listingId={}", request.listingId());
        VehicleListingEvent event = eventFactory.fromPriceUpdateRequest(request);
        return publisher.publish(event);
    }

    @PostMapping("/sold")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EventPublishResponse markSold(@Valid @RequestBody SoldListingRequest request) {
        log.info("Sold listing request received for listingId={}", request.listingId());
        VehicleListingEvent event = eventFactory.fromSoldRequest(request);
        return publisher.publish(event);
    }
}
