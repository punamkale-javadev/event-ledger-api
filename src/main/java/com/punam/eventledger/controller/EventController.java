package com.punam.eventledger.controller;

import com.punam.eventledger.dto.BalanceResponse;
import com.punam.eventledger.dto.EventRequest;
import com.punam.eventledger.dto.EventResponse;
import com.punam.eventledger.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class EventController {

    private final EventService eventService;

    @PostMapping("/events")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request) {

        EventResponse response =
                eventService.createEvent(request);

        HttpStatus status =
                response.isDuplicate()
                        ? HttpStatus.OK
                        : HttpStatus.CREATED;

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EventResponse>
    getEvent(
            @PathVariable String id) {

        return ResponseEntity.ok(
                eventService.getEvent(id));
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventResponse>>
    getEvents(
            @RequestParam("account")
            String accountId) {

        return ResponseEntity.ok(
                eventService.getEvents(
                        accountId));
    }

    @GetMapping(
            "/accounts/{accountId}/balance")
    public ResponseEntity<BalanceResponse>
    getBalance(
            @PathVariable String accountId) {

        return ResponseEntity.ok(
                eventService.getBalance(
                        accountId));
    }
}