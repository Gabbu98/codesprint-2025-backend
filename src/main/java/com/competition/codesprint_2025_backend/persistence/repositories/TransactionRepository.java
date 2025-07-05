package com.competition.codesprint_2025_backend.persistence.repositories;

import com.competition.codesprint_2025_backend.persistence.models.TransactionModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository extends MongoRepository<TransactionModel, String> {

    List<TransactionModel> findByType(String type);
}
