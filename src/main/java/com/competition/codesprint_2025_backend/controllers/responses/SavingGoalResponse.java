package com.competition.codesprint_2025_backend.controllers.responses;

import com.competition.codesprint_2025_backend.persistence.models.SavingGoalModel;

import java.math.BigDecimal;

/**
 * Response model for saving goal operations
 */
public class SavingGoalResponse {

    private String id;
    private String name;
    private BigDecimal target;
    private BigDecimal saved;
    private BigDecimal remainder;
    private double progressPercentage;

    // Private constructor to force use of factory methods
    private SavingGoalResponse(String id, String name, BigDecimal target,
                               BigDecimal saved, BigDecimal remainder, double progressPercentage) {
        this.id = id;
        this.name = name;
        this.target = target;
        this.saved = saved;
        this.remainder = remainder;
        this.progressPercentage = progressPercentage;
    }

    // Factory method to create from SavingGoalModel
    public static SavingGoalResponse from(SavingGoalModel model) {
        double progressPercentage = calculateProgressPercentage(model.getSaved(), model.getTarget());

        return new SavingGoalResponse(
                model.getId(),
                model.getName(),
                model.getTarget(),
                model.getSaved(),
                model.getRemainder(),
                progressPercentage
        );
    }

    // Factory method to create from individual values
    public static SavingGoalResponse of(String id, String name, BigDecimal target,
                                        BigDecimal saved, BigDecimal remainder) {
        double progressPercentage = calculateProgressPercentage(saved, target);

        return new SavingGoalResponse(id, name, target, saved, remainder, progressPercentage);
    }

    // Helper method to calculate progress percentage
    private static double calculateProgressPercentage(BigDecimal saved, BigDecimal target) {
        if (target.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        BigDecimal percentage = saved
                .divide(target, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return Math.round(percentage.doubleValue() * 100.0) / 100.0;
    }

    // Getters only (immutable response)
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getTarget() {
        return target;
    }

    public BigDecimal getSaved() {
        return saved;
    }

    public BigDecimal getRemainder() {
        return remainder;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    @Override
    public String toString() {
        return "SavingGoalResponse{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", target=" + target +
                ", saved=" + saved +
                ", remainder=" + remainder +
                ", progressPercentage=" + progressPercentage +
                '}';
    }
}
