package com.competition.codesprint_2025_backend.businessServices;

import com.competition.codesprint_2025_backend.controllers.responses.SpendingRecommendation;
import com.competition.codesprint_2025_backend.persistence.models.TransactionModel;
import com.competition.codesprint_2025_backend.services.TransactionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIService {

    private ChatClient chatClient;
    private TransactionService transactionService;

    public AIService(ChatClient chatClient, TransactionService transactionService) {
        this.chatClient = chatClient;
        this.transactionService = transactionService;
    }

    /**
     * Gets AI-powered spending recommendations based on category percentages and trends
     */
    public SpendingRecommendation getSpendingRecommendations() {
        // Get current data
        Map<String, Double> categoryPercentages = transactionService.getCategorySpendingPercentagesForPieChart();
        Map<String, Double> monthlyTrends = transactionService.getMonthlySpendingTotals();
        Map<String, Map<String, Double>> categoryTrends = getCategoryTrendsOverTime();

        // Create analysis prompt
        String analysisPrompt = buildAnalysisPrompt(categoryPercentages, monthlyTrends, categoryTrends);

        // Get AI recommendation
        String aiResponse = chatClient.prompt()
                .user(analysisPrompt)
                .call()
                .content();

        return new SpendingRecommendation(
                aiResponse,
                extractPriorityCategory(categoryPercentages),
                extractActionableSteps(aiResponse)
        );
    }

    /**
     * Gets category spending trends over the last 6 months
     */
    private Map<String, Map<String, Double>> getCategoryTrendsOverTime() {
        List<TransactionModel> debitTransactions = transactionService.getByType("debit");

        // Group by month and category
        Map<String, Map<String, BigDecimal>> monthlyCategories = debitTransactions.stream()
                .collect(Collectors.groupingBy(
                        transaction -> formatTransactionMonth(transaction.getDate()),
                        Collectors.groupingBy(
                                transaction -> transaction.getCategory() != null ? transaction.getCategory() : "other",
                                Collectors.reducing(BigDecimal.ZERO, TransactionModel::getAmount, BigDecimal::add)
                        )
                ));

        // Convert to percentages for each month
        Map<String, Map<String, Double>> trends = new HashMap<>();
        monthlyCategories.forEach((month, categories) -> {
            BigDecimal monthTotal = categories.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (!monthTotal.equals(BigDecimal.ZERO)) {
                Map<String, Double> monthPercentages = new HashMap<>();
                categories.forEach((category, amount) -> {
                    double percentage = amount.divide(monthTotal, 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    monthPercentages.put(category, Math.round(percentage * 100.0) / 100.0);
                });
                trends.put(month, monthPercentages);
            }
        });

        return trends;
    }

    /**
     * Builds the AI analysis prompt with spending data
     */
    private String buildAnalysisPrompt(Map<String, Double> categoryPercentages,
                                       Map<String, Double> monthlyTrends,
                                       Map<String, Map<String, Double>> categoryTrends) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a personal finance advisor. Analyze the following spending data and tell me in one or two sentences which category is the most problematic and during which month and provide actionable recommendations in bullet points\n\n");

        // Current category breakdown
        prompt.append("CURRENT SPENDING BREAKDOWN BY CATEGORY:\n");
        categoryPercentages.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(entry -> prompt.append(String.format("- %s: %.2f%%\n",
                        formatCategoryName(entry.getKey()), entry.getValue())));

        // Monthly spending trends
        prompt.append("\nMONTHLY SPENDING TOTALS:\n");
        monthlyTrends.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> prompt.append(String.format("- %s: €%.2f\n",
                        entry.getKey(), entry.getValue())));

        // Category trends over time
        prompt.append("\nCATEGORY TRENDS OVER TIME:\n");
        categoryTrends.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(monthEntry -> {
                    prompt.append(String.format("%s: ", monthEntry.getKey()));
                    monthEntry.getValue().forEach((category, percentage) ->
                            prompt.append(String.format("%s: %.1f%%, ", formatCategoryName(category), percentage)));
                    prompt.append("\n");
                });

        prompt.append("\nBased on this data, please provide:\n");
        prompt.append("1. PRIORITY CATEGORY: Which category should I focus on reducing first and why?\n");
        prompt.append("2. SPECIFIC RECOMMENDATIONS: 3-5 actionable steps I can take immediately\n");
        prompt.append("3. TREND ANALYSIS: What concerning trends do you see?\n");
        prompt.append("4. SAVINGS POTENTIAL: Estimate how much I could save per month\n");
        prompt.append("\nProvide a clear, structured response with practical advice.");

        return prompt.toString();
    }

    /**
     * Extracts the priority category from current percentages (highest spending)
     */
    private String extractPriorityCategory(Map<String, Double> categoryPercentages) {
        return categoryPercentages.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey())
                .orElse("unknown");
    }

    /**
     * Extracts actionable steps from AI response (simple parsing)
     */
    private List<String> extractActionableSteps(String aiResponse) {
        List<String> steps = new ArrayList<>();
        String[] lines = aiResponse.split("\n");

        boolean inRecommendations = false;
        for (String line : lines) {
            if (line.toLowerCase().contains("recommendation") || line.toLowerCase().contains("steps")) {
                inRecommendations = true;
                continue;
            }

            if (inRecommendations && (line.trim().startsWith("-") || line.trim().matches("^\\d+\\."))) {
                steps.add(line.trim().replaceFirst("^[-\\d\\.\\s]+", ""));
            }

            if (inRecommendations && line.toLowerCase().contains("trend") && !steps.isEmpty()) {
                break; // Stop at next section
            }
        }

        return steps.isEmpty() ? Arrays.asList("Review spending habits", "Set monthly budgets", "Track expenses daily") : steps;
    }

    /**
     * Helper methods
     */
    private String formatTransactionMonth(Instant date) {
        return DateTimeFormatter.ofPattern("yyyy-MM")
                .format(date.atOffset(ZoneOffset.UTC));
    }

    private String formatCategoryName(String category) {
        return Arrays.stream(category.replace("_", " ").split(" "))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
