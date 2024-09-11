package com.example.demo.repository;

import com.example.demo.entities.Paiement;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaiementRepo extends MongoRepository<Paiement, String> {
}
