package com.example.demo.aggregate;

import com.example.demo.command.PaiementCreateCommand;
import com.example.demo.event.PaiementCreated;
import com.example.demo.entities.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaiementAggregate {

    private UUID id;
    private String description;
    private BigDecimal montant;
    private LocalDate date;
    private StatusEnum status;

    public List<Object> handle(PaiementCreateCommand command) {
        List<Object> events = new ArrayList<>();
        PaiementCreated paiementCreated = new PaiementCreated(
                command.getId(), command.getDescription(), command.getMontant(), command.getDate(), StatusEnum.IN_PROGRESS
        );

        events.add(paiementCreated);
        apply(paiementCreated);
        return events;
    }

    public void apply(PaiementCreated event) {
        this.id = event.getId();
        this.description = event.getDescription();
        this.montant = event.getMontant();
        this.date = event.getDate();
        this.status = event.getStatus();
    }
}
