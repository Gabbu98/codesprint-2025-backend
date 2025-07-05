package com.competition.codesprint_2025_backend.controllers;

import com.competition.codesprint_2025_backend.controllers.responses.TransactionCategoryResponse;
import com.competition.codesprint_2025_backend.mappers.TransactionsMapper;
import com.competition.codesprint_2025_backend.persistence.models.AlertModel;
import com.competition.codesprint_2025_backend.persistence.repositories.AlertRepository;
import com.competition.codesprint_2025_backend.services.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v0/alerts")
public class AlertsController {

    private AlertRepository alertRepository;

    public AlertsController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @GetMapping()
    public ResponseEntity<AlertModel> getSpendingPercentages(){
        return ResponseEntity.ok(this.alertRepository.findTopByOrderByTimestampDesc().orElse(null));
    }

}
