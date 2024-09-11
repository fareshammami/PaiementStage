package com.example.demo.command;

import com.example.demo.entities.StatusEnum;
import com.example.demo.event.PaiementValidated;
import com.example.demo.configs.EventStoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PaiementCommandHandler {

    @Autowired
    private EventStoreRepository eventStoreRepository;

    public CompletableFuture<Void> handleValidation(PaiementValidateCommand command) throws Exception {
        PaiementValidated paiementValidated = new PaiementValidated(
                command.getId(),
                "", // Description
                BigDecimal.ZERO, // Montant
                LocalDate.now(), // Date
                StatusEnum.VALIDATED
        );

        return eventStoreRepository.saveEvent(command.getId(), paiementValidated)
                .thenApply(result -> null);
    }
}
