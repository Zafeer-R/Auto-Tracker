package com.vehiclelisting.consumer.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehiclelisting.consumer.entity.ListingHistory;
import com.vehiclelisting.consumer.mapper.ListingHistoryMapper;
import com.vehiclelisting.consumer.repository.ListingHistoryRepository;
import com.vehiclelisting.consumer.validation.VehicleListingEventValidator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

class VehicleListingConsumerTest {

    private final ListingHistoryRepository repository = mock(ListingHistoryRepository.class);
    private final DlqPublisher dlqPublisher = mock(DlqPublisher.class);
    private final VehicleListingConsumer consumer = new VehicleListingConsumer(
            new ObjectMapper(),
            new VehicleListingEventValidator(),
            new ListingHistoryMapper(Clock.fixed(Instant.parse("2026-04-30T15:00:00Z"), ZoneOffset.UTC)),
            repository,
            dlqPublisher
    );

    @Test
    void shouldPersistValidListingCreatedEvent() {
        consumer.process("""
                {
                  "eventId": "evt-1001-created",
                  "eventType": "LISTING_CREATED",
                  "listingId": "LST-1001",
                  "vin": "1HGCM82633A004352",
                  "make": "Toyota",
                  "model": "Camry",
                  "year": 2022,
                  "price": 24500.00,
                  "previousPrice": null,
                  "status": "ACTIVE",
                  "dealerId": "DLR-2001",
                  "eventTimestamp": "2026-04-30T14:25:00Z"
                }
                """);

        ArgumentCaptor<ListingHistory> captor = ArgumentCaptor.forClass(ListingHistory.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo("evt-1001-created");
        assertThat(captor.getValue().getListingId()).isEqualTo("LST-1001");
        assertThat(captor.getValue().getEventTimestamp()).isEqualTo(Instant.parse("2026-04-30T14:25:00Z"));
        assertThat(captor.getValue().getProcessedAt()).isEqualTo(Instant.parse("2026-04-30T15:00:00Z"));
        verify(dlqPublisher, never()).publish(any(), any());
    }

    @Test
    void shouldSendInvalidEventToDlq() {
        String payload = """
                {
                  "eventId": "evt-invalid",
                  "eventType": "LISTING_CREATED",
                  "vin": "1HGCM82633A004352",
                  "make": "Toyota",
                  "model": "Camry",
                  "year": 2022,
                  "price": 24500.00,
                  "status": "ACTIVE",
                  "dealerId": "DLR-2001",
                  "eventTimestamp": "2026-04-30T14:25:00Z"
                }
                """;

        consumer.process(payload);

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(dlqPublisher).publish(eq(payload), contains("listingId"));
    }

    @Test
    void shouldSendMalformedJsonToDlq() {
        String payload = "{ invalid-json";

        consumer.process(payload);

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(dlqPublisher).publish(eq(payload), contains("Invalid JSON"));
    }

    @Test
    void shouldSendDatabaseFailureToDlq() {
        String payload = """
                {
                  "eventId": "evt-1001-created",
                  "eventType": "LISTING_CREATED",
                  "listingId": "LST-1001",
                  "vin": "1HGCM82633A004352",
                  "make": "Toyota",
                  "model": "Camry",
                  "year": 2022,
                  "price": 24500.00,
                  "previousPrice": null,
                  "status": "ACTIVE",
                  "dealerId": "DLR-2001",
                  "eventTimestamp": "2026-04-30T14:25:00Z"
                }
                """;
        doThrow(new DataIntegrityViolationException("duplicate event")).when(repository).save(any());

        consumer.process(payload);

        verify(dlqPublisher).publish(eq(payload), contains("Database persistence failed"));
    }
}
