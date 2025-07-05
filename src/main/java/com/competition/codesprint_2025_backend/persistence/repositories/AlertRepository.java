package com.competition.codesprint_2025_backend.persistence.repositories;

import com.competition.codesprint_2025_backend.persistence.models.AlertModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AlertRepository extends MongoRepository<AlertModel, String> {
    Optional<AlertModel> findTopByOrderByTimestampDesc();
}
