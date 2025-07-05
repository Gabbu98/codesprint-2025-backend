package com.competition.codesprint_2025_backend.controllers.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request model for creating a new saving goal
 * Maps to: addSavingGoal(String name, BigDecimal target, BigDecimal saved)
 */
public class CreateSavingGoalRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be greater than 0")
    private BigDecimal target;

    @NotNull(message = "Saved amount is required")
    @DecimalMin(value = "0.0", message = "Saved amount cannot be negative")
    private BigDecimal saved;

    // Default constructor
    public CreateSavingGoalRequest() {}

    // Constructor
    public CreateSavingGoalRequest(String name, BigDecimal target, BigDecimal saved) {
        this.name = name;
        this.target = target;
        this.saved = saved;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTarget() {
        return target;
    }

    public void setTarget(BigDecimal target) {
        this.target = target;
    }

    public BigDecimal getSaved() {
        return saved;
    }

    public void setSaved(BigDecimal saved) {
        this.saved = saved;
    }

    @Override
    public String toString() {
        return "CreateSavingGoalRequest{" +
                "name='" + name + '\'' +
                ", target=" + target +
                ", saved=" + saved +
                '}';
    }
}