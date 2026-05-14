package com.vehiclelisting.consumer.service;

import com.vehiclelisting.consumer.dto.ListingHistoryResponse;
import com.vehiclelisting.consumer.entity.ListingHistory;
import com.vehiclelisting.consumer.model.VehicleListingEventType;
import com.vehiclelisting.consumer.repository.ListingHistoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListingHistoryService {

    private static final Sort EVENT_TIMESTAMP_ASC = Sort.by(Sort.Direction.ASC, "eventTimestamp");

    private final ListingHistoryRepository repository;

    public ListingHistoryService(ListingHistoryRepository repository) {
        this.repository = repository;
    }

    public List<ListingHistoryResponse> getAllHistory() {
        return repository.findAll(EVENT_TIMESTAMP_ASC)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ListingHistoryResponse> getHistoryByListingId(String listingId) {
        return repository.findByListingIdOrderByEventTimestampAsc(listingId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ListingHistoryResponse> getHistoryByEventType(String eventType) {
        VehicleListingEventType.valueOf(eventType);
        return repository.findByEventTypeOrderByEventTimestampAsc(eventType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ListingHistoryResponse> getHistoryByDealerId(String dealerId) {
        return repository.findByDealerIdOrderByEventTimestampAsc(dealerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ListingHistoryResponse> getRecentHistory() {
        return repository.findTop20ByOrderByProcessedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ListingHistoryResponse toResponse(ListingHistory history) {
        return new ListingHistoryResponse(
                history.getEventId(),
                history.getEventType(),
                history.getListingId(),
                history.getVin(),
                history.getMake(),
                history.getModel(),
                history.getVehicleYear(),
                history.getPrice(),
                history.getPreviousPrice(),
                history.getStatus(),
                history.getDealerId(),
                history.getEventTimestamp(),
                history.getProcessedAt()
        );
    }
}
