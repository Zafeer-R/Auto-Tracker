package com.vehiclelisting.consumer.controller;

import com.vehiclelisting.consumer.dto.ListingHistoryResponse;
import com.vehiclelisting.consumer.service.ListingHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ListingHistoryController.class)
class ListingHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListingHistoryService service;

    @Test
    void shouldReturnAllHistory() throws Exception {
        when(service.getAllHistory()).thenReturn(List.of(response("evt-1", "LISTING_CREATED")));

        mockMvc.perform(get("/api/listings/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value("evt-1"))
                .andExpect(jsonPath("$[0].listingId").value("LST-1001"))
                .andExpect(jsonPath("$[0].year").value(2022));
    }

    @Test
    void shouldReturnHistoryByListingId() throws Exception {
        when(service.getHistoryByListingId("LST-1001")).thenReturn(List.of(response("evt-2", "PRICE_UPDATED")));

        mockMvc.perform(get("/api/listings/LST-1001/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("PRICE_UPDATED"));
    }

    @Test
    void shouldReturnHistoryByEventType() throws Exception {
        when(service.getHistoryByEventType("LISTING_SOLD")).thenReturn(List.of(response("evt-3", "LISTING_SOLD")));

        mockMvc.perform(get("/api/listings/history/event-type/LISTING_SOLD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("LISTING_SOLD"));
    }

    @Test
    void shouldReturnBadRequestForInvalidEventType() throws Exception {
        when(service.getHistoryByEventType("BAD_TYPE")).thenThrow(new IllegalArgumentException("No enum constant"));

        mockMvc.perform(get("/api/listings/history/event-type/BAD_TYPE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturnCorsHeaderForFrontendOrigin() throws Exception {
        when(service.getRecentHistory()).thenReturn(List.of());

        mockMvc.perform(get("/api/listings/history/recent").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    private ListingHistoryResponse response(String eventId, String eventType) {
        return new ListingHistoryResponse(
                eventId,
                eventType,
                "LST-1001",
                "1HGCM82633A004352",
                "Toyota",
                "Camry",
                2022,
                new BigDecimal("24500.00"),
                null,
                "ACTIVE",
                "DLR-2001",
                Instant.parse("2026-04-30T14:25:00Z"),
                Instant.parse("2026-04-30T15:00:00Z")
        );
    }
}
