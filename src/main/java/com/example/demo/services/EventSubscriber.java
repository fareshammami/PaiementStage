package com.example.demo.services;

import com.eventstore.dbclient.*;
import com.example.demo.entities.StatusEnum;
import com.example.demo.event.PaiementCreated;
import com.example.demo.event.PaiementValidated;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class EventSubscriber {

    @Autowired
    private EventStoreDBClient eventStore;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String STREAM_NAME = "dewdrop.IndemnisationAggregate-f47ac10b-58cc-4372-a567-0e02b6f3d479";

    @PostConstruct
    public void subscribeToEvents() {
        eventStore.subscribeToStream(
                STREAM_NAME,
                new SubscriptionListener() {
                    @Override
                    public void onEvent(Subscription subscription, ResolvedEvent event) {
                        try {
                            String eventType = event.getOriginalEvent().getEventType();
                            if ("PaiementCreated".equals(eventType)) {
                                byte[] eventDataBytes = event.getOriginalEvent().getEventData();
                                String eventData = new String(eventDataBytes, StandardCharsets.UTF_8);
                                PaiementCreated paiementCreated = objectMapper.readValue(eventData, PaiementCreated.class);

                                // Check if the paiement is IN_PROGRESS
                                if (paiementCreated.getStatus() == StatusEnum.IN_PROGRESS) {
                                    // Validate the Paiement
                                    validatePaiement(paiementCreated);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(Subscription subscription, Throwable throwable) {
                        throwable.printStackTrace();
                    }
                },
                SubscribeToStreamOptions.get()
        );
    }

    private void validatePaiement(PaiementCreated paiementCreated) {
        try {
            PaiementValidated paiementValidated = new PaiementValidated(
                    paiementCreated.getId(),
                    paiementCreated.getDescription(),
                    paiementCreated.getMontant(),
                    paiementCreated.getDate(),
                    StatusEnum.VALIDATED
            );

            // Append validation event to the EventStore
            savePaiementValidatedEvent(paiementValidated);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePaiementValidatedEvent(PaiementValidated event) throws Exception {
        String eventType = event.getClass().getSimpleName();
        String eventJsonData = objectMapper.writeValueAsString(event);
        EventData eventData = EventData.builderAsJson(eventType, eventJsonData.getBytes(StandardCharsets.UTF_8))
                .eventId(UUID.randomUUID())
                .build();

        eventStore.appendToStream(
                STREAM_NAME,
                AppendToStreamOptions.get().expectedRevision(ExpectedRevision.streamExists()),
                eventData
        ).get();
    }
}
