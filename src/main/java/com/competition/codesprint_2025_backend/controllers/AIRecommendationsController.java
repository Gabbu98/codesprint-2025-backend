package com.competition.codesprint_2025_backend.controllers;

import com.competition.codesprint_2025_backend.businessServices.AIService;
import com.competition.codesprint_2025_backend.controllers.responses.SpendingRecommendation;
import com.competition.codesprint_2025_backend.controllers.responses.TransactionCategoryResponse;
import com.competition.codesprint_2025_backend.mappers.TransactionsMapper;
import com.competition.codesprint_2025_backend.services.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v0/ai")
public class AIRecommendationsController {

    private AIService aiService;

    public AIRecommendationsController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<SpendingRecommendation> getSpendingRecommendations(){
        return ResponseEntity.ok(aiService.getSpendingRecommendations());
    }
}
