package com.example.demo.services;

import com.example.demo.aggregate.PaiementAggregate;
import com.example.demo.command.PaiementCreateCommand;
import com.example.demo.configs.EventStoreRepository;
import com.example.demo.entities.StatusEnum;
import com.example.demo.event.PaiementCreated;
import com.example.demo.entities.Paiement;
import com.example.demo.event.PaiementValidated;
import com.example.demo.repository.PaiementRepo;
import com.example.demo.event.PaiementEventHandlers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PaiementService {

    @Autowired
    private EventStoreRepository eventStoreRepository;

    @Autowired
    private PaiementRepo paiementRepo;

    @Autowired
    private PaiementEventHandlers paiementEventHandler;

    private static final String STREAM_NAME = "dewdrop.IndemnisationAggregate-f47ac10b-58cc-4372-a567-0e02b6f3d479";

    public CompletableFuture<Void> createPaiement(PaiementCreateCommand command) throws Exception {
        PaiementAggregate aggregate = new PaiementAggregate();
        List<Object> events = aggregate.handle(command);

        // Save events to EventStore
        return eventStoreRepository.save(command.getId(), events)
                .thenAccept(v -> {
                    // Apply events to the aggregate
                    events.forEach(event -> {
                        if (event instanceof PaiementCreated) {
                            aggregate.apply((PaiementCreated) event);
                            paiementEventHandler.handle((PaiementCreated) event);
                        }
                    });
                });
    }

    public List<Paiement> getAllPaiements() {
        return paiementRepo.findAll();
    }

    public Paiement getPaiementById(String id) {
        return paiementRepo.findById(id).orElse(null);
    }

    public CompletableFuture<Void> validateLastPaiementIfNeeded() {
        return eventStoreRepository.getLastEvent(STREAM_NAME)
                .thenCompose(event -> {
                    if (event == null) {
                        System.err.println("No event was found in the event store.");
                        return CompletableFuture.completedFuture(null); // No event to process
                    }

                    try {
                        // Log the type of the last event
                        System.out.println("Last event type: " + event.getClass().getSimpleName());
                        System.out.println("Last event data: " + event.toString());

                        if (event instanceof PaiementCreated) {
                            PaiementCreated paiementCreated = (PaiementCreated) event;

                            // Check if the paiement is IN_PROGRESS
                            if (paiementCreated.getStatus() == StatusEnum.IN_PROGRESS) {
                                PaiementValidated paiementValidated = new PaiementValidated(
                                        paiementCreated.getId(),
                                        paiementCreated.getDescription(),
                                        paiementCreated.getMontant(),
                                        paiementCreated.getDate(),
                                        StatusEnum.VALIDATED
                                );

                                // Append validation event to the EventStore
                                return eventStoreRepository.savePaiementValidated(paiementCreated.getId(), paiementValidated)
                                        .thenAccept(v -> {
                                            paiementEventHandler.handle(paiementValidated);
                                            System.out.println("Validation successful for Paiement ID: " + paiementCreated.getId());
                                        })
                                        .exceptionally(ex -> {
                                            System.err.println("Error saving PaiementValidated event: " + ex.getMessage());
                                            return null;
                                        });
                            } else {
                                System.out.println("Paiement is not in IN_PROGRESS state, validation skipped.");
                                return CompletableFuture.completedFuture(null);
                            }
                        } else {
                            System.out.println("Last event is not of type PaiementCreated, validation skipped.");
                            return CompletableFuture.completedFuture(null);
                        }
                    } catch (Exception e) {
                        System.err.println("Exception occurred during event handling: " + e.getMessage());
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Error retrieving or processing last event: " + e.getMessage());
                    return null;
                });
    }
    public CompletableFuture<Void> validatePaiementAutomatically(PaiementCreated paiementCreated) throws Exception {
        // Check if the paiement is IN_PROGRESS
        if (paiementCreated.getStatus() == StatusEnum.IN_PROGRESS) {
            PaiementValidated paiementValidated = new PaiementValidated(
                    paiementCreated.getId(),
                    paiementCreated.getDescription(),
                    paiementCreated.getMontant(),
                    paiementCreated.getDate(),
                    StatusEnum.VALIDATED
            );

            // Append validation event to the EventStore
            return eventStoreRepository.savePaiementValidated(paiementCreated.getId(), paiementValidated)
                    .thenAccept(v -> {
                        paiementEventHandler.handle(paiementValidated);
                        System.out.println("Paiement automatically validated for ID: " + paiementCreated.getId());
                    })
                    .exceptionally(ex -> {
                        System.err.println("Error saving PaiementValidated event: " + ex.getMessage());
                        return null;
                    });
        } else {
            System.out.println("Paiement is not in IN_PROGRESS state, validation skipped.");
            return CompletableFuture.completedFuture(null);
        }
    }



}
