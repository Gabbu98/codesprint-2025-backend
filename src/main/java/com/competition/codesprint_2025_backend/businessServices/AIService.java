package com.competition.codesprint_2025_backend.businessServices;

import com.competition.codesprint_2025_backend.controllers.responses.SpendingRecommendation;
import com.competition.codesprint_2025_backend.persistence.models.TransactionModel;
import com.competition.codesprint_2025_backend.persistence.models.SavingGoalModel;
import com.competition.codesprint_2025_backend.persistence.repositories.SavingGoalRepository;
import com.competition.codesprint_2025_backend.services.TransactionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIService {

    private ChatClient chatClient;
    private TransactionService transactionService;
    private SavingGoalRepository savingGoalRepository;

    // Conversation context to maintain state
    private final Map<String, List<ChatMessage>> conversationHistory = new HashMap<>();

    public AIService(ChatClient chatClient, TransactionService transactionService, SavingGoalRepository savingGoalRepository) {
        this.chatClient = chatClient;
        this.transactionService = transactionService;
        this.savingGoalRepository = savingGoalRepository;
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
     * Main chatbot interaction method
     */
    public ChatbotResponse chat(String sessionId, String userMessage) {
        try {
            // Get or create conversation history
            List<ChatMessage> history = conversationHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());

            // Add user message to history
            history.add(new ChatMessage("user", userMessage, Instant.now()));

            // Check if user is asking for specific financial data
            String contextualData = extractRelevantFinancialData(userMessage);

            // Build the prompt with context
            String enhancedPrompt = buildChatPrompt(userMessage, contextualData, history);

            // Get AI response
            String aiResponse = chatClient.prompt()
                    .user(enhancedPrompt)
                    .call()
                    .content();

            // Add AI response to history
            history.add(new ChatMessage("assistant", aiResponse, Instant.now()));

            // Limit conversation history to last 10 messages to manage memory
            if (history.size() > 10) {
                history.subList(0, history.size() - 10).clear();
            }

            return new ChatbotResponse(aiResponse, sessionId, true, extractSuggestions(aiResponse));

        } catch (Exception e) {
            return new ChatbotResponse(
                    "I'm sorry, I'm having trouble processing your request right now. Please try again.",
                    sessionId,
                    false,
                    Arrays.asList("Try asking about your spending", "Check your savings goals")
            );
        }
    }

    /**
     * Extract relevant financial data based on user query
     */
    private String extractRelevantFinancialData(String userMessage) {
        StringBuilder context = new StringBuilder();
        String lowerMessage = userMessage.toLowerCase();

        // If asking about spending/categories
        if (lowerMessage.contains("spending") || lowerMessage.contains("categories") ||
                lowerMessage.contains("budget") || lowerMessage.contains("expense")) {

            Map<String, Double> categoryPercentages = transactionService.getCategorySpendingPercentagesForPieChart();
            Map<String, Double> monthlyTrends = transactionService.getMonthlySpendingTotals();

            context.append("CURRENT SPENDING BREAKDOWN:\n");
            categoryPercentages.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(5)
                    .forEach(entry -> context.append(String.format("- %s: %.1f%%\n",
                            formatCategoryName(entry.getKey()), entry.getValue())));

            context.append("\nRECENT MONTHLY TOTALS:\n");
            monthlyTrends.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> context.append(String.format("- %s: €%.2f\n",
                            entry.getKey(), entry.getValue())));
        }

        // If asking about savings goals
        if (lowerMessage.contains("savings") || lowerMessage.contains("goals") ||
                lowerMessage.contains("target")) {

            List<SavingGoalModel> savingGoals = savingGoalRepository.findAll();
            context.append("\nCURRENT SAVINGS GOALS:\n");
            savingGoals.forEach(goal -> {
                double progress = goal.getTarget().doubleValue() > 0 ?
                        (goal.getSaved().doubleValue() / goal.getTarget().doubleValue()) * 100 : 0;
                context.append(String.format("- %s: €%.2f / €%.2f (%.1f%% complete)\n",
                        goal.getName(), goal.getSaved(), goal.getTarget(), progress));
            });
        }

        // If asking about recent transactions
        if (lowerMessage.contains("recent") || lowerMessage.contains("latest") ||
                lowerMessage.contains("transaction")) {

            List<TransactionModel> recentTransactions = transactionService.getRecentTransactions(10);
            context.append("\nRECENT TRANSACTIONS:\n");
            recentTransactions.stream()
                    .limit(5)
                    .forEach(tx -> context.append(String.format("- %s: €%.2f (%s)\n",
                            tx.getDescription(), tx.getAmount(), tx.getCategory())));
        }

        return context.toString();
    }

    /**
     * Build enhanced prompt for chatbot with financial context
     */
    private String buildChatPrompt(String userMessage, String contextualData, List<ChatMessage> history) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a helpful personal finance assistant. You help users understand their spending, ");
        prompt.append("manage their budget, and achieve their savings goals. Be conversational, friendly, and provide actionable advice.\n\n");

        // Add conversation history for context
        if (!history.isEmpty()) {
            prompt.append("CONVERSATION HISTORY:\n");
            history.stream()
                    .skip(Math.max(0, history.size() - 4)) // Last 4 messages
                    .forEach(msg -> prompt.append(String.format("%s: %s\n",
                            msg.getRole().toUpperCase(), msg.getContent())));
            prompt.append("\n");
        }

        // Add financial data if relevant
        if (!contextualData.trim().isEmpty()) {
            prompt.append("RELEVANT FINANCIAL DATA:\n");
            prompt.append(contextualData);
            prompt.append("\n");
        }

        prompt.append("USER QUESTION: ").append(userMessage).append("\n\n");
        prompt.append("Please provide a helpful response. If the user is asking about their financial data, ");
        prompt.append("reference the specific numbers provided above. Keep responses concise but informative.");

        return prompt.toString();
    }

    /**
     * Extract actionable suggestions from AI response
     */
    private List<String> extractSuggestions(String aiResponse) {
        List<String> suggestions = new ArrayList<>();

        if (aiResponse.toLowerCase().contains("spending")) {
            suggestions.add("Show my spending breakdown");
        }
        if (aiResponse.toLowerCase().contains("savings") || aiResponse.toLowerCase().contains("goal")) {
            suggestions.add("Check my savings goals");
        }
        if (aiResponse.toLowerCase().contains("budget")) {
            suggestions.add("Help me create a budget");
        }
        if (aiResponse.toLowerCase().contains("recommendation")) {
            suggestions.add("Get personalized recommendations");
        }

        // Default suggestions if none extracted
        if (suggestions.isEmpty()) {
            suggestions.addAll(Arrays.asList(
                    "What's my biggest spending category?",
                    "How are my savings goals doing?",
                    "Show me this month's spending",
                    "Give me money-saving tips"
            ));
        }

        return suggestions;
    }

    /**
     * Get predefined quick responses for common questions
     */
    public List<String> getQuickResponses() {
        return Arrays.asList(
                "What's my biggest spending category?",
                "How much did I spend this month?",
                "How are my savings goals doing?",
                "Give me tips to save money",
                "Show me my recent transactions",
                "Help me create a budget",
                "What should I focus on reducing?"
        );
    }

    /**
     * Clear conversation history for a session
     */
    public void clearConversation(String sessionId) {
        conversationHistory.remove(sessionId);
    }

    /**
     * Get conversation history for a session
     */
    public List<ChatMessage> getConversationHistory(String sessionId) {
        return conversationHistory.getOrDefault(sessionId, new ArrayList<>());
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

    /**
     * Chat message model for conversation history
     */
    public static class ChatMessage {
        private final String role;
        private final String content;
        private final Instant timestamp;

        public ChatMessage(String role, String content, Instant timestamp) {
            this.role = role;
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
        public Instant getTimestamp() { return timestamp; }
    }

    /**
     * Chatbot response model
     */
    public static class ChatbotResponse {
        private final String message;
        private final String sessionId;
        private final boolean success;
        private final List<String> suggestions;
        private final Instant timestamp;

        public ChatbotResponse(String message, String sessionId, boolean success, List<String> suggestions) {
            this.message = message;
            this.sessionId = sessionId;
            this.success = success;
            this.suggestions = suggestions;
            this.timestamp = Instant.now();
        }

        public String getMessage() { return message; }
        public String getSessionId() { return sessionId; }
        public boolean isSuccess() { return success; }
        public List<String> getSuggestions() { return suggestions; }
        public Instant getTimestamp() { return timestamp; }
    }
}