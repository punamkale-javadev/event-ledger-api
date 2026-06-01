package com.punam.eventledger.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.punam.eventledger.dto.EventRequest;
import com.punam.eventledger.entity.EventType;
import com.punam.eventledger.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    private EventRequest buildRequest(
            String eventId,
            String accountId,
            EventType type,
            BigDecimal amount,
            String timestamp) {

        EventRequest request = new EventRequest();
        request.setEventId(eventId);
        request.setAccountId(accountId);
        request.setType(type);
        request.setAmount(amount);
        request.setCurrency("USD");
        request.setEventTimestamp(
                Instant.parse(timestamp));

        return request;
    }

    @Test
    void shouldCreateEvent() throws Exception {

        EventRequest request =
                buildRequest(
                        "evt-001",
                        "acct-123",
                        EventType.CREDIT,
                        BigDecimal.valueOf(100),
                        "2026-05-15T10:00:00Z");

        mockMvc.perform(
                        post("/v1/events")
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper
                                                .writeValueAsString(request)))
                .andExpect(status().isCreated());

        org.junit.jupiter.api.Assertions.assertEquals(
                1,
                repository.count());
    }
    //1. Idempotency test - Duplicate eventId should not create another event.
    @Test
    void shouldHandleDuplicateSubmission() throws Exception {

        EventRequest request =
                buildRequest(
                        "evt-001",
                        "acct-123",
                        EventType.CREDIT,
                        BigDecimal.valueOf(100),
                        "2026-05-15T10:00:00Z");

        mockMvc.perform(post("/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertEquals(
                1,
                repository.count());
    }
//2. Out-of-Order Event Arrival
    @Test
    void shouldReturnEventsOrderedByTimestamp()
            throws Exception {

        EventRequest e1 =
                buildRequest(
                        "evt-001",
                        "acct-123",
                        EventType.CREDIT,
                        BigDecimal.valueOf(100),
                        "2026-05-15T10:00:00Z");

        EventRequest e2 =
                buildRequest(
                        "evt-002",
                        "acct-123",
                        EventType.DEBIT,
                        BigDecimal.valueOf(20),
                        "2026-05-15T12:00:00Z");

        EventRequest e3 =
                buildRequest(
                        "evt-003",
                        "acct-123",
                        EventType.CREDIT,
                        BigDecimal.valueOf(50),
                        "2026-05-15T09:00:00Z");

        mockMvc.perform(post("/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(e1)));

        mockMvc.perform(post("/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(e2)));

        mockMvc.perform(post("/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(e3)));

        mockMvc.perform(
                        get("/v1/events")
                                .param("account",
                                        "acct-123"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].eventId")
                                .value("evt-003"))
                .andExpect(
                        jsonPath("$[1].eventId")
                                .value("evt-001"))
                .andExpect(
                        jsonPath("$[2].eventId")
                                .value("evt-002"));
    }


    //3. Balance Computation
    @Test
    void shouldCalculateBalanceCorrectly()
            throws Exception {

        mockMvc.perform(post("/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        buildRequest(
                                "evt-001",
                                "acct-123",
                                EventType.CREDIT,
                                BigDecimal.valueOf(100),
                                "2026-05-15T10:00:00Z"))));

        mockMvc.perform(post("/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        buildRequest(
                                "evt-002",
                                "acct-123",
                                EventType.DEBIT,
                                BigDecimal.valueOf(20),
                                "2026-05-15T11:00:00Z"))));

        mockMvc.perform(post("/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        buildRequest(
                                "evt-003",
                                "acct-123",
                                EventType.CREDIT,
                                BigDecimal.valueOf(50),
                                "2026-05-15T12:00:00Z"))));

        mockMvc.perform(
                        get("/v1/accounts/acct-123/balance"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.balance")
                                .value(130));
    }

    //4. Validation Test
    @Test
    void shouldRejectNegativeAmount()
            throws Exception {

        EventRequest request =
                buildRequest(
                        "evt-001",
                        "acct-123",
                        EventType.CREDIT,
                        BigDecimal.valueOf(-10),
                        "2026-05-15T10:00:00Z");

        mockMvc.perform(post("/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    //5. Event Not Found
    @Test
    void shouldReturn404WhenEventMissing()
            throws Exception {

        mockMvc.perform(
                        get("/v1/events/unknown"))
                .andExpect(
                        status().isNotFound());
    }


}