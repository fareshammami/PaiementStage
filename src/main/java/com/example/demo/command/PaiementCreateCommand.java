package com.example.demo.command;

import com.example.demo.entities.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementCreateCommand {
    private UUID id;
    private String description;
    private BigDecimal montant;
    private LocalDate date;
    private StatusEnum status = StatusEnum.IN_PROGRESS; // Default status
}
