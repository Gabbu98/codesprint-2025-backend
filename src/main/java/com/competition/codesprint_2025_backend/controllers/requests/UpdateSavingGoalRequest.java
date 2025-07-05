package com.competition.codesprint_2025_backend.controllers.requests;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class UpdateSavingGoalRequest {

    @NotBlank(message = "Saved amount is required")
    private BigDecimal saved;

    // Default constructor
    public UpdateSavingGoalRequest() {}

    // Constructor
    public UpdateSavingGoalRequest(BigDecimal saved) {
        this.saved = saved;
    }

    // Getters and Setters
    public BigDecimal getSaved() {
        return saved;
    }

    public void setSaved(BigDecimal saved) {
        this.saved = saved;
    }

    @Override
    public String toString() {
        return "UpdateSavingGoalRequest{" +
                "saved='" + saved + '\'' +
                '}';
    }
}
