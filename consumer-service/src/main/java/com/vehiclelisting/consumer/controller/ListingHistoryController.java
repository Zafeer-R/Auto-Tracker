package com.vehiclelisting.consumer.controller;

import com.vehiclelisting.consumer.dto.ListingHistoryResponse;
import com.vehiclelisting.consumer.service.ListingHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/listings")
public class ListingHistoryController {

    private final ListingHistoryService service;

    public ListingHistoryController(ListingHistoryService service) {
        this.service = service;
    }

    @GetMapping("/history")
    public List<ListingHistoryResponse> getAllHistory() {
        return service.getAllHistory();
    }

    @GetMapping("/{listingId}/history")
    public List<ListingHistoryResponse> getHistoryByListingId(@PathVariable String listingId) {
        return service.getHistoryByListingId(listingId);
    }

    @GetMapping("/history/event-type/{eventType}")
    public List<ListingHistoryResponse> getHistoryByEventType(@PathVariable String eventType) {
        return service.getHistoryByEventType(eventType);
    }

    @GetMapping("/history/dealer/{dealerId}")
    public List<ListingHistoryResponse> getHistoryByDealerId(@PathVariable String dealerId) {
        return service.getHistoryByDealerId(dealerId);
    }

    @GetMapping("/history/recent")
    public List<ListingHistoryResponse> getRecentHistory() {
        return service.getRecentHistory();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidArgument(IllegalArgumentException exception) {
        return Map.of("error", "Invalid request parameter: " + exception.getMessage());
    }
}
