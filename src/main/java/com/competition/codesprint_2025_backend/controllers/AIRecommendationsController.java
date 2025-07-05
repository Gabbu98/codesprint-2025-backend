package com.competition.codesprint_2025_backend.controllers;

import com.competition.codesprint_2025_backend.businessServices.AIService;
import com.competition.codesprint_2025_backend.controllers.requests.ChatRequest;
import com.competition.codesprint_2025_backend.controllers.responses.SpendingRecommendation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v0/ai")
public class AIRecommendationsController {

    private AIService aiService;

    public AIRecommendationsController(AIService aiService) {
        this.aiService = aiService;
    }

    /**
     * Get AI-powered spending recommendations
     * GET /v0/ai/recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<SpendingRecommendation> getSpendingRecommendations() {
        return ResponseEntity.ok(aiService.getSpendingRecommendations());
    }

    /**
     * Chat with the financial assistant
     * POST /v0/ai/chat
     */
    @PostMapping("/chat")
    public ResponseEntity<AIService.ChatbotResponse> chat(@Valid @RequestBody ChatRequest request) {
        AIService.ChatbotResponse response = aiService.chat(request.getSessionId(), request.getMessage());
        return ResponseEntity.ok(response);
    }

    /**
     * Get quick response suggestions for common questions
     * GET /v0/ai/quick-responses
     */
    @GetMapping("/quick-responses")
    public ResponseEntity<List<String>> getQuickResponses() {
        List<String> quickResponses = aiService.getQuickResponses();
        return ResponseEntity.ok(quickResponses);
    }

    /**
     * Get conversation history for a specific session
     * GET /v0/ai/chat/history/{sessionId}
     */
    @GetMapping("/chat/history/{sessionId}")
    public ResponseEntity<List<AIService.ChatMessage>> getChatHistory(@PathVariable String sessionId) {
        List<AIService.ChatMessage> history = aiService.getConversationHistory(sessionId);
        return ResponseEntity.ok(history);
    }

    /**
     * Clear conversation history for a specific session
     * DELETE /v0/ai/chat/history/{sessionId}
     */
    @DeleteMapping("/chat/history/{sessionId}")
    public ResponseEntity<Void> clearChatHistory(@PathVariable String sessionId) {
        aiService.clearConversation(sessionId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get AI analysis for a specific financial topic
     * GET /v0/ai/analyze?topic={topic}
     */
    @GetMapping("/analyze")
    public ResponseEntity<AIService.ChatbotResponse> analyzeFinancialTopic(@RequestParam String topic) {
        // Generate a session ID for this analysis
        String analysisSessionId = "analysis_" + System.currentTimeMillis();

        // Create analysis prompt based on topic
        String analysisMessage = buildAnalysisMessage(topic);

        AIService.ChatbotResponse response = aiService.chat(analysisSessionId, analysisMessage);
        return ResponseEntity.ok(response);
    }

    /**
     * Get personalized financial advice based on spending patterns
     * GET /v0/ai/advice?category={category}
     */
    @GetMapping("/advice")
    public ResponseEntity<AIService.ChatbotResponse> getFinancialAdvice(
            @RequestParam(required = false) String category) {

        String adviceSessionId = "advice_" + System.currentTimeMillis();
        String adviceMessage = buildAdviceMessage(category);

        AIService.ChatbotResponse response = aiService.chat(adviceSessionId, adviceMessage);
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to build analysis message based on topic
     */
    private String buildAnalysisMessage(String topic) {
        return switch (topic.toLowerCase()) {
            case "spending" -> "Analyze my current spending patterns and tell me what concerns you the most.";
            case "savings" -> "How are my savings goals progressing and what can I do to improve?";
            case "budget" -> "Help me create a realistic monthly budget based on my spending history.";
            case "trends" -> "What trends do you see in my spending over the past few months?";
            case "categories" -> "Which spending categories should I focus on reducing and why?";
            case "goals" -> "Evaluate my savings goals and suggest optimizations.";
            default -> "Give me a comprehensive financial health check based on all my data.";
        };
    }

    /**
     * Helper method to build advice message for specific category
     */
    private String buildAdviceMessage(String category) {
        if (category != null && !category.trim().isEmpty()) {
            return String.format("Give me specific advice on how to reduce my %s spending. " +
                    "What are practical steps I can take immediately?", category.replace("_", " "));
        } else {
            return "Based on my spending patterns, what are the top 3 areas where I can save money? " +
                    "Give me specific, actionable advice.";
        }
    }
}