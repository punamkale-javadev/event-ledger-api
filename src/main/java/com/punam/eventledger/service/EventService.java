package com.punam.eventledger.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.punam.eventledger.dto.BalanceResponse;
import com.punam.eventledger.dto.EventRequest;
import com.punam.eventledger.dto.EventResponse;
import com.punam.eventledger.entity.Event;
import com.punam.eventledger.exception.EventNotFoundException;
import com.punam.eventledger.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {
    private final ObjectMapper objectMapper;
    private final EventRepository repository;

    public EventResponse createEvent(
            EventRequest request) {

        Optional<Event> existing =
                repository.findByEventId(
                        request.getEventId());

        if (existing.isPresent()) {

            EventResponse response =
                    mapToResponse(existing.get());

            response.setDuplicate(true);

            return response;
        }

        try {
            String metadataJson = null;

            if (request.getMetadata() != null) {
                metadataJson =
                        objectMapper.writeValueAsString(
                                request.getMetadata());
            }
            Event entity =
                    Event.builder()
                            .eventId(
                                    request.getEventId())
                            .accountId(
                                    request.getAccountId())
                            .type(
                                    request.getType())
                            .amount(
                                    request.getAmount())
                            .currency(
                                    request.getCurrency())
                            .eventTimestamp(
                                    request.getEventTimestamp())
                            .metadata(metadataJson)
                            .build();

            Event saved =
                    repository.save(entity);

            EventResponse response =
                    mapToResponse(saved);

            response.setDuplicate(false);

            return response;

        } catch (
                DataIntegrityViolationException ex) {

            Event event =
                    repository.findByEventId(
                                    request.getEventId())
                            .orElseThrow();

            return mapToResponse(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public EventResponse getEvent(
            String eventId) {

        Event event =
                repository.findByEventId(eventId)
                        .orElseThrow(() ->
                                new EventNotFoundException(
                                        "Event not found"));

        return mapToResponse(event);
    }

    public List<EventResponse> getEvents(
            String accountId) {

        return repository
                .findByAccountIdOrderByEventTimestampAsc(
                        accountId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BalanceResponse getBalance(
            String accountId) {

        BigDecimal balance =
                repository.calculateBalance(
                        accountId);

        return BalanceResponse.builder()
                .accountId(accountId)
                .balance(balance)
                .build();
    }

    private EventResponse mapToResponse(
            Event entity) {

        return EventResponse.builder()
                .eventId(entity.getEventId())
                .accountId(entity.getAccountId())
                .type(entity.getType())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .eventTimestamp(
                        entity.getEventTimestamp())
                .build();
    }
}