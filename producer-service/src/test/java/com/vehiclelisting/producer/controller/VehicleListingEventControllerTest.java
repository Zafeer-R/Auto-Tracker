package com.vehiclelisting.producer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehiclelisting.producer.dto.EventPublishResponse;
import com.vehiclelisting.producer.model.VehicleListingEvent;
import com.vehiclelisting.producer.service.VehicleListingEventFactory;
import com.vehiclelisting.producer.service.VehicleListingPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleListingEventController.class)
class VehicleListingEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleListingEventFactory eventFactory;

    @MockBean
    private VehicleListingPublisher publisher;

    @Test
    void shouldAcceptCreateListingRequest() throws Exception {
        VehicleListingEvent event = testEvent("LISTING_CREATED", "ACTIVE");
        when(eventFactory.fromCreateRequest(any())).thenReturn(event);
        when(publisher.publish(event)).thenReturn(
                EventPublishResponse.accepted("vehicle-listing-events", "LISTING_CREATED", "LST-1001")
        );

        mockMvc.perform(post("/api/listings/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateListingPayload())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.topic").value("vehicle-listing-events"))
                .andExpect(jsonPath("$.eventType").value("LISTING_CREATED"))
                .andExpect(jsonPath("$.listingId").value("LST-1001"))
                .andExpect(jsonPath("$.message").value("Vehicle listing event accepted for publishing"));
    }

    @Test
    void shouldRejectCreateListingRequestMissingRequiredField() throws Exception {
        String invalidPayload = """
                {
                  "vin": "1HGCM82633A004352",
                  "make": "Toyota",
                  "model": "Camry",
                  "year": 2022,
                  "price": 24500.00,
                  "dealerId": "DLR-2001"
                }
                """;

        mockMvc.perform(post("/api/listings/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAcceptPriceUpdateRequest() throws Exception {
        VehicleListingEvent event = testEvent("PRICE_UPDATED", "UPDATED");
        when(eventFactory.fromPriceUpdateRequest(any())).thenReturn(event);
        when(publisher.publish(event)).thenReturn(
                EventPublishResponse.accepted("vehicle-listing-events", "PRICE_UPDATED", "LST-1001")
        );

        mockMvc.perform(post("/api/listings/events/price-update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listingId": "LST-1001",
                                  "vin": "1HGCM82633A004352",
                                  "make": "Toyota",
                                  "model": "Camry",
                                  "year": 2022,
                                  "previousPrice": 24500.00,
                                  "price": 23900.00,
                                  "dealerId": "DLR-2001"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventType").value("PRICE_UPDATED"));
    }

    @Test
    void shouldAcceptSoldListingRequest() throws Exception {
        VehicleListingEvent event = testEvent("LISTING_SOLD", "SOLD");
        when(eventFactory.fromSoldRequest(any())).thenReturn(event);
        when(publisher.publish(event)).thenReturn(
                EventPublishResponse.accepted("vehicle-listing-events", "LISTING_SOLD", "LST-1001")
        );

        mockMvc.perform(post("/api/listings/events/sold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "listingId": "LST-1001",
                                  "vin": "1HGCM82633A004352",
                                  "make": "Toyota",
                                  "model": "Camry",
                                  "year": 2022,
                                  "price": 23900.00,
                                  "dealerId": "DLR-2001"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventType").value("LISTING_SOLD"));
    }

    @Test
    void shouldRejectNegativePrice() throws Exception {
        String invalidPayload = """
                {
                  "listingId": "LST-1001",
                  "vin": "1HGCM82633A004352",
                  "make": "Toyota",
                  "model": "Camry",
                  "year": 2022,
                  "price": -1.00,
                  "dealerId": "DLR-2001"
                }
                """;

        mockMvc.perform(post("/api/listings/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPassValidatedRequestToFactory() throws Exception {
        VehicleListingEvent event = testEvent("LISTING_CREATED", "ACTIVE");
        when(eventFactory.fromCreateRequest(any())).thenReturn(event);
        when(publisher.publish(event)).thenReturn(
                EventPublishResponse.accepted("vehicle-listing-events", "LISTING_CREATED", "LST-1001")
        );
        ArgumentCaptor<com.vehiclelisting.producer.dto.CreateListingRequest> captor =
                ArgumentCaptor.forClass(com.vehiclelisting.producer.dto.CreateListingRequest.class);

        mockMvc.perform(post("/api/listings/events/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateListingPayload())))
                .andExpect(status().isAccepted());

        org.mockito.Mockito.verify(eventFactory).fromCreateRequest(captor.capture());
        assertThat(captor.getValue().listingId()).isEqualTo("LST-1001");
        assertThat(captor.getValue().price()).isEqualByComparingTo("24500.00");
    }

    private VehicleListingEvent testEvent(String eventType, String status) {
        return new VehicleListingEvent(
                "evt-1",
                eventType,
                "LST-1001",
                "1HGCM82633A004352",
                "Toyota",
                "Camry",
                2022,
                new BigDecimal("24500.00"),
                null,
                status,
                "DLR-2001",
                "2026-04-30T14:25:00Z"
        );
    }

    private record CreateListingPayload(
            String listingId,
            String vin,
            String make,
            String model,
            Integer year,
            BigDecimal price,
            String dealerId
    ) {
        private CreateListingPayload() {
            this(
                    "LST-1001",
                    "1HGCM82633A004352",
                    "Toyota",
                    "Camry",
                    2022,
                    new BigDecimal("24500.00"),
                    "DLR-2001"
            );
        }
    }
}
