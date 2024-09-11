package com.example.demo.configs;

import com.eventstore.dbclient.*;
import com.example.demo.event.PaiementCreated;
import com.example.demo.event.PaiementValidated;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Repository
public class EventStoreRepository {

    @Autowired
    private EventStoreDBClient eventStore;

    @Autowired
    private ObjectMapper objectMapper;

    // Save multiple events to the event store
    public CompletableFuture<Void> save(UUID aggregateId, List<Object> events) throws Exception {
        List<CompletableFuture<WriteResult>> futures = new ArrayList<>();

        for (Object event : events) {
            String eventType = event.getClass().getSimpleName();
            String eventJsonData = objectMapper.writeValueAsString(event);
            EventData eventData = EventData.builderAsJson(eventType, eventJsonData.getBytes(StandardCharsets.UTF_8))
                    .eventId(UUID.randomUUID())
                    .build();

            CompletableFuture<WriteResult> future = eventStore.appendToStream(
                    aggregateId.toString(),
                    AppendToStreamOptions.get().expectedRevision(ExpectedRevision.noStream()),
                    eventData
            );

            futures.add(future);
        }

        // Combine all futures into one
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    // Save a single event to the event store
    public CompletableFuture<WriteResult> saveEvent(UUID aggregateId, Object event) throws Exception {
        String eventType = event.getClass().getSimpleName();
        String eventJsonData = objectMapper.writeValueAsString(event);
        EventData eventData = EventData.builderAsJson(eventType, eventJsonData.getBytes(StandardCharsets.UTF_8))
                .eventId(UUID.randomUUID())
                .build();

        return eventStore.appendToStream(
                aggregateId.toString(),
                AppendToStreamOptions.get().expectedRevision(ExpectedRevision.noStream()),
                eventData
        );
    }

    // Retrieve events from the event store
    public CompletableFuture<List<Object>> getEvents(UUID aggregateId) {
        return eventStore.readStream(aggregateId.toString(), ReadStreamOptions.get().fromStart())
                .thenApply(result -> {
                    List<Object> events = new ArrayList<>();
                    for (ResolvedEvent resolvedEvent : result.getEvents()) {
                        String eventType = resolvedEvent.getOriginalEvent().getEventType();
                        byte[] eventDataBytes = resolvedEvent.getOriginalEvent().getEventData();
                        String eventData = new String(eventDataBytes, StandardCharsets.UTF_8);
                        try {
                            Class<?> eventClass = Class.forName("com.example.demo.event." + eventType);
                            Object event = objectMapper.readValue(eventData, eventClass);
                            events.add(event);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to deserialize event", e);
                        }
                    }
                    return events;
                })
                .exceptionally(ex -> {
                    throw new RuntimeException("Error reading events from stream", ex);
                });
    }

    // Retrieve all events from a specific stream
    public CompletableFuture<List<Object>> getAllEvents(String streamName) {
        return eventStore.readStream(streamName, ReadStreamOptions.get().fromStart())
                .thenApply(result -> {
                    List<Object> events = new ArrayList<>();
                    for (ResolvedEvent resolvedEvent : result.getEvents()) {
                        String eventType = resolvedEvent.getOriginalEvent().getEventType();
                        byte[] eventDataBytes = resolvedEvent.getOriginalEvent().getEventData();
                        String eventData = new String(eventDataBytes, StandardCharsets.UTF_8);
                        try {
                            Class<?> eventClass = Class.forName("com.example.demo.event." + eventType);
                            Object event = objectMapper.readValue(eventData, eventClass);
                            events.add(event);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to deserialize event", e);
                        }
                    }
                    return events;
                })
                .exceptionally(ex -> {
                    throw new RuntimeException("Error reading events from stream", ex);
                });
    }

    // Retrieve the last event from a specific stream
    public CompletableFuture<PaiementCreated> getLastEvent(String streamName) {
        return eventStore.readStream(streamName, ReadStreamOptions.get().fromStart())
                .thenApply(result -> {
                    if (result.getEvents().isEmpty()) {
                        return null; // Return null if there are no events
                    }
                    ResolvedEvent lastEvent = result.getEvents().get(result.getEvents().size() - 1);
                    String eventType = lastEvent.getOriginalEvent().getEventType();
                    String eventData = new String(lastEvent.getOriginalEvent().getEventData(), StandardCharsets.UTF_8);

                    // Log event details
                    System.out.println("Last event type: " + eventType);
                    System.out.println("Last event data: " + eventData);

                    try {
                        if (eventType.equals("PaiementCreated")) {
                            return objectMapper.readValue(eventData, PaiementCreated.class);
                        } else {
                            return null; // Return null for other event types
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse event data", e);
                    }
                })
                .exceptionally(ex -> {
                    throw new RuntimeException("Error reading last event from stream", ex);
                });
    }

    // Save PaiementValidated event to the event store
    public CompletableFuture<Void> savePaiementValidated(UUID aggregateId, PaiementValidated event) throws Exception {
        String eventType = event.getClass().getSimpleName();
        String eventJsonData = objectMapper.writeValueAsString(event);
        EventData eventData = EventData.builderAsJson(eventType, eventJsonData.getBytes(StandardCharsets.UTF_8))
                .eventId(UUID.randomUUID())
                .build();

        return eventStore.appendToStream(
                aggregateId.toString(),
                AppendToStreamOptions.get().expectedRevision(ExpectedRevision.any()),
                eventData
        ).thenAccept(result -> {
            // Optionally handle the result or log
            System.out.println("PaiementValidated event saved for ID: " + aggregateId);
        }).exceptionally(ex -> {
            throw new RuntimeException("Error saving PaiementValidated event", ex);
        });
    }
}
