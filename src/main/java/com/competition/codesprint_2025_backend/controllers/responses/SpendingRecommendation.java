package com.competition.codesprint_2025_backend.controllers.responses;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SpendingRecommendation {
    private Map<String, Double> categoryPercentages;
    private Map<String, Double> monthlyTrends;
    private String aiAnalysis;
    private String priorityCategory;
    private List<String> actionableSteps;

    private SpendingRecommendation() {
    }

    public SpendingRecommendation(Map<String, Double> categoryPercentages,
                                  Map<String, Double> monthlyTrends,
                                  String aiAnalysis,
                                  String priorityCategory,
                                  List<String> actionableSteps) {
        this.categoryPercentages = categoryPercentages;
        this.monthlyTrends = monthlyTrends;
        this.aiAnalysis = aiAnalysis;
        this.priorityCategory = priorityCategory;
        this.actionableSteps = actionableSteps;
    }

    public Map<String, Double> getCategoryPercentages() {
        return categoryPercentages;
    }

    public SpendingRecommendation setCategoryPercentages(Map<String, Double> categoryPercentages) {
        this.categoryPercentages = categoryPercentages;
        return this;
    }

    public Map<String, Double> getMonthlyTrends() {
        return monthlyTrends;
    }

    public SpendingRecommendation setMonthlyTrends(Map<String, Double> monthlyTrends) {
        this.monthlyTrends = monthlyTrends;
        return this;
    }

    public String getAiAnalysis() {
        return aiAnalysis;
    }

    public SpendingRecommendation setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
        return this;
    }

    public String getPriorityCategory() {
        return priorityCategory;
    }

    public SpendingRecommendation setPriorityCategory(String priorityCategory) {
        this.priorityCategory = priorityCategory;
        return this;
    }

    public List<String> getActionableSteps() {
        return actionableSteps;
    }

    public SpendingRecommendation setActionableSteps(List<String> actionableSteps) {
        this.actionableSteps = actionableSteps;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SpendingRecommendation that = (SpendingRecommendation) o;
        return Objects.equals(categoryPercentages, that.categoryPercentages) && Objects.equals(monthlyTrends, that.monthlyTrends) && Objects.equals(aiAnalysis, that.aiAnalysis) && Objects.equals(priorityCategory, that.priorityCategory) && Objects.equals(actionableSteps, that.actionableSteps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryPercentages, monthlyTrends, aiAnalysis, priorityCategory, actionableSteps);
    }

    @Override
    public String toString() {
        return "SpendingRecommendation{" +
                "categoryPercentages=" + categoryPercentages +
                ", monthlyTrends=" + monthlyTrends +
                ", aiAnalysis='" + aiAnalysis + '\'' +
                ", priorityCategory='" + priorityCategory + '\'' +
                ", actionableSteps=" + actionableSteps +
                '}';
    }
}
