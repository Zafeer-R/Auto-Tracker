package com.vehiclelisting.consumer.repository;

import com.vehiclelisting.consumer.entity.ListingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingHistoryRepository extends JpaRepository<ListingHistory, Long> {

    List<ListingHistory> findByListingIdOrderByEventTimestampAsc(String listingId);

    List<ListingHistory> findByEventTypeOrderByEventTimestampAsc(String eventType);

    List<ListingHistory> findByDealerIdOrderByEventTimestampAsc(String dealerId);

    List<ListingHistory> findTop20ByOrderByProcessedAtDesc();
}
