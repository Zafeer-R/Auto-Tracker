package com.vehiclelisting.consumer.service;

import com.vehiclelisting.consumer.dto.ListingHistoryResponse;
import com.vehiclelisting.consumer.entity.ListingHistory;
import com.vehiclelisting.consumer.repository.ListingHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListingHistoryServiceTest {

    private final ListingHistoryRepository repository = mock(ListingHistoryRepository.class);
    private final ListingHistoryService service = new ListingHistoryService(repository);

    @Test
    void shouldReturnAllHistorySortedByEventTimestamp() {
        ListingHistory history = listingHistory("evt-1", "LISTING_CREATED");
        when(repository.findAll(Sort.by(Sort.Direction.ASC, "eventTimestamp"))).thenReturn(List.of(history));

        List<ListingHistoryResponse> response = service.getAllHistory();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).eventId()).isEqualTo("evt-1");
        assertThat(response.get(0).year()).isEqualTo(2022);
        verify(repository).findAll(Sort.by(Sort.Direction.ASC, "eventTimestamp"));
    }

    @Test
    void shouldReturnHistoryByListingId() {
        when(repository.findByListingIdOrderByEventTimestampAsc("LST-1001"))
                .thenReturn(List.of(listingHistory("evt-2", "PRICE_UPDATED")));

        List<ListingHistoryResponse> response = service.getHistoryByListingId("LST-1001");

        assertThat(response).hasSize(1);
        assertThat(response.get(0).listingId()).isEqualTo("LST-1001");
        assertThat(response.get(0).eventType()).isEqualTo("PRICE_UPDATED");
    }

    @Test
    void shouldReturnHistoryByValidEventType() {
        when(repository.findByEventTypeOrderByEventTimestampAsc("LISTING_SOLD"))
                .thenReturn(List.of(listingHistory("evt-3", "LISTING_SOLD")));

        List<ListingHistoryResponse> response = service.getHistoryByEventType("LISTING_SOLD");

        assertThat(response).hasSize(1);
        assertThat(response.get(0).eventType()).isEqualTo("LISTING_SOLD");
    }

    @Test
    void shouldRejectInvalidEventTypeFilter() {
        assertThatThrownBy(() -> service.getHistoryByEventType("BAD_TYPE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnRecentHistory() {
        when(repository.findTop20ByOrderByProcessedAtDesc())
                .thenReturn(List.of(listingHistory("evt-4", "LISTING_CREATED")));

        List<ListingHistoryResponse> response = service.getRecentHistory();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).processedAt()).isEqualTo(Instant.parse("2026-04-30T15:00:00Z"));
    }

    private ListingHistory listingHistory(String eventId, String eventType) {
        ListingHistory history = new ListingHistory();
        history.setEventId(eventId);
        history.setEventType(eventType);
        history.setListingId("LST-1001");
        history.setVin("1HGCM82633A004352");
        history.setMake("Toyota");
        history.setModel("Camry");
        history.setVehicleYear(2022);
        history.setPrice(new BigDecimal("24500.00"));
        history.setPreviousPrice(new BigDecimal("24000.00"));
        history.setStatus("ACTIVE");
        history.setDealerId("DLR-2001");
        history.setEventTimestamp(Instant.parse("2026-04-30T14:25:00Z"));
        history.setProcessedAt(Instant.parse("2026-04-30T15:00:00Z"));
        return history;
    }
}
