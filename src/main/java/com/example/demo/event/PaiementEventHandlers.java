package com.example.demo.event;

import com.example.demo.entities.Paiement;
import com.example.demo.entities.StatusEnum;
import com.example.demo.repository.PaiementRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaiementEventHandlers {

    @Autowired
    private PaiementRepo paiementRepo;

    public void handle(PaiementCreated event) {
        Paiement paiement = new Paiement();
        paiement.setId(event.getId().toString());
        paiement.setDescription(event.getDescription());
        paiement.setMontant(event.getMontant());
        paiement.setDate(event.getDate());
        paiement.setStatus(event.getStatus());
        paiementRepo.save(paiement);
    }

    public void handle(PaiementValidated event) {
        Paiement paiement = paiementRepo.findById(event.getId().toString()).orElse(null);
        if (paiement != null) {
            paiement.setStatus(StatusEnum.VALIDATED);
            paiementRepo.save(paiement);
        }
    }
}
