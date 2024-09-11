package com.example.demo.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Paiements")
public class Paiement {
    private String id; // Identifiant unique du paiement
    private String description; // Description du paiement
    private BigDecimal montant; // Montant du paiement
    private LocalDate date; // Date du paiement
    private StatusEnum status; // Status du paiement
}
