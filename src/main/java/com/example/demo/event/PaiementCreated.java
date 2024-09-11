package com.example.demo.event;

import com.example.demo.entities.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementCreated {
    private UUID id;
    private String description;
    private BigDecimal montant;
    private LocalDate date;
    private StatusEnum status; // Add status attribute
}
