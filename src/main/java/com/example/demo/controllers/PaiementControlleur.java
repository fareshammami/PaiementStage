package com.example.demo.controllers;

import com.example.demo.command.PaiementCreateCommand;
import com.example.demo.entities.Paiement;
import com.example.demo.services.PaiementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/paiement")
public class PaiementControlleur {

    @Autowired
    private PaiementService paiementService;

    @PostMapping("/create")
    public CompletableFuture<ResponseEntity<String>> createPaiement(@RequestBody PaiementCreateCommand command) throws Exception {
        command.setId(UUID.randomUUID()); // Generate a new UUID for the command
        return paiementService.createPaiement(command)
                .thenApply(v -> ResponseEntity.status(HttpStatus.CREATED).body("Paiement created successfully"))
                .exceptionally(e -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error creating paiement: " + e.getMessage()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Paiement>> getAllPaiements() {
        return ResponseEntity.ok(paiementService.getAllPaiements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Paiement> getPaiementById(@PathVariable String id) {
        Paiement paiement = paiementService.getPaiementById(id);
        return paiement != null ? ResponseEntity.ok(paiement) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/validate-last")
    public CompletableFuture<ResponseEntity<String>> validateLastPaiementIfNeeded() throws Exception {
        return paiementService.validateLastPaiementIfNeeded()
                .thenApply(v -> ResponseEntity.status(HttpStatus.OK).body("Paiement validation check completed"))
                .exceptionally(e -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error validating last paiement: " + e.getMessage()));
    }
}
