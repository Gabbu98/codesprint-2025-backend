package com.competition.codesprint_2025_backend.configurations;

import com.competition.codesprint_2025_backend.persistence.models.AlertModel;
import com.competition.codesprint_2025_backend.persistence.models.TransactionModel;
import com.competition.codesprint_2025_backend.persistence.repositories.AlertRepository;
import com.competition.codesprint_2025_backend.persistence.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
@EnableScheduling
public class AlertScheduler {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Value("${whatsapp.token}")
    private String whatsappApiToken;

    @Value("${whatsapp.to}")
    private String recipientPhoneNumber;

    @Value("${whatsapp.accountId}")
    private String whatsappAccountId;

    @Value("${alert.loss.threshold:0.00}")
    private BigDecimal lossThreshold;

    @Value("${alert.enabled:true}")
    private boolean alertEnabled;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @EventListener(ApplicationReadyEvent.class)
    public void applicationReadyNotification() {
        System.out.println("AlertScheduler: ApplicationReadyEvent triggered at " + LocalDateTime.now().format(FORMATTER));

        try {
            // Send startup notification
            String startupMessage = "🚀 Transaction Alert System has started successfully!\n" +
                    "Monitoring transactions for loss detection.\n" +
                    "Time: " + LocalDateTime.now().format(FORMATTER);
            sendWhatsAppMessage(startupMessage, "Application Ready Alert");

            // Perform initial analysis of today's transactions
            if (alertEnabled) {
                LocalDate today = LocalDate.now();
                TransactionSummary todaySummary = calculateTransactionSummary(today, today);

                if (todaySummary.getTotalTransactions() > 0) {
                    String todayMessage = formatSummaryMessage(todaySummary, "Today");
                    sendWhatsAppMessage(todayMessage, "Initial Transaction Summary");
                }
            }

        } catch (Exception e) {
            System.err.println("AlertScheduler: Error during application ready notification: " + e.getMessage());
        }
    }

    /**
     * This method is scheduled to run every day at 9:00 AM (09:00:00).
     * It calculates transaction balance and sends alert if there's a loss.
     */
    @Scheduled(cron = "0 0 9 * * ?") // Daily at 9:00 AM
    public void dailyTransactionAnalysis() {
        System.out.println("AlertScheduler: Daily transaction analysis triggered at " + LocalDateTime.now().format(FORMATTER));

        if (!alertEnabled) {
            System.out.println("AlertScheduler: Alerts are disabled, skipping analysis");
            return;
        }

        try {
            // Analyze yesterday's transactions
            LocalDate yesterday = LocalDate.now().minusDays(1);
            TransactionSummary summary = calculateTransactionSummary(yesterday, yesterday);

            if (summary.isLoss() && summary.getLossAmount().abs().compareTo(lossThreshold) > 0) {
                String message = formatLossAlert(summary, "Daily");
                sendWhatsAppMessage(message, "Daily Transaction Loss Alert");
            } else if (summary.isProfit()) {
                String message = formatProfitMessage(summary, "Daily");
                sendWhatsAppMessage(message, "Daily Transaction Profit Alert");
            } else {
                System.out.println("AlertScheduler: No significant changes in daily transactions");
            }

        } catch (Exception e) {
            System.err.println("AlertScheduler: Error during daily transaction analysis: " + e.getMessage());
            String errorMessage = "Error occurred during daily transaction analysis: " + e.getMessage();
            sendWhatsAppMessage(errorMessage, "Daily Analysis Error Alert");
        }
    }

    /**
     * Calculate transaction summary for a given date range
     */
    private TransactionSummary calculateTransactionSummary(LocalDate startDate, LocalDate endDate) {
        List<TransactionModel> transactions = transactionRepository.findAll();

        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalDebit = BigDecimal.ZERO;
        int transactionCount = transactions.size();

        for (TransactionModel transaction : transactions) {
            if (transaction.getType() != null) {
                switch (transaction.getType().toUpperCase()) {
                    case "CREDIT":
                        totalCredit = totalCredit.add(transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO);
                        break;
                    case "DEBIT":
                        totalDebit = totalDebit.add(transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO);
                        break;
                }
            }
        }

        BigDecimal balance = totalCredit.subtract(totalDebit);

        return new TransactionSummary(
                startDate, endDate, totalCredit, totalDebit, balance, transactionCount
        );
    }

    /**
     * Format loss alert message
     */
    private String formatLossAlert(TransactionSummary summary, String period) {
        return String.format(
                "⚠️ %s LOSS ALERT ⚠️\n\n" +
                        "📅 Period: %s to %s\n" +
                        "💰 Total Credits: $%.2f\n" +
                        "💸 Total Debits: $%.2f\n" +
                        "📉 Net Loss: $%.2f\n" +
                        "📊 Transactions: %d\n\n" +
                        "🔍 Please review your transactions immediately!",
                period.toUpperCase(),
                summary.getStartDate().format(DATE_FORMATTER),
                summary.getEndDate().format(DATE_FORMATTER),
                summary.getTotalCredit(),
                summary.getTotalDebit(),
                summary.getBalance().abs(),
                summary.getTotalTransactions()
        );
    }

    /**
     * Format profit message
     */
    private String formatProfitMessage(TransactionSummary summary, String period) {
        return String.format(
                "✅ %s PROFIT UPDATE\n\n" +
                        "📅 Period: %s to %s\n" +
                        "💰 Total Credits: $%.2f\n" +
                        "💸 Total Debits: $%.2f\n" +
                        "📈 Net Profit: $%.2f\n" +
                        "📊 Transactions: %d\n\n" +
                        "Great job! Keep it up! 🎉",
                period.toUpperCase(),
                summary.getStartDate().format(DATE_FORMATTER),
                summary.getEndDate().format(DATE_FORMATTER),
                summary.getTotalCredit(),
                summary.getTotalDebit(),
                summary.getBalance(),
                summary.getTotalTransactions()
        );
    }

    /**
     * Format summary message
     */
    private String formatSummaryMessage(TransactionSummary summary, String period) {
        String status = summary.isLoss() ? "📉 Loss" : summary.isProfit() ? "📈 Profit" : "⚖️ Break Even";

        return String.format(
                "📊 %s TRANSACTION SUMMARY\n\n" +
                        "📅 Period: %s to %s\n" +
                        "💰 Total Credits: $%.2f\n" +
                        "💸 Total Debits: $%.2f\n" +
                        "💵 Net Balance: $%.2f\n" +
                        "📊 Transactions: %d\n" +
                        "📋 Status: %s",
                period.toUpperCase(),
                summary.getStartDate().format(DATE_FORMATTER),
                summary.getEndDate().format(DATE_FORMATTER),
                summary.getTotalCredit(),
                summary.getTotalDebit(),
                summary.getBalance(),
                summary.getTotalTransactions(),
                status
        );
    }

    /**
     * Format detailed summary message for monthly reports
     */
    private String formatDetailedSummaryMessage(TransactionSummary summary, String period) {
        String status = summary.isLoss() ? "📉 Loss" : summary.isProfit() ? "📈 Profit" : "⚖️ Break Even";
        BigDecimal avgDailyBalance = summary.getTotalTransactions() > 0 ?
                summary.getBalance().divide(BigDecimal.valueOf(summary.getStartDate().until(summary.getEndDate()).getDays() + 1), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;

        return String.format(
                "📈 %s DETAILED REPORT\n\n" +
                        "📅 Period: %s to %s\n" +
                        "💰 Total Credits: $%.2f\n" +
                        "💸 Total Debits: $%.2f\n" +
                        "💵 Net Balance: $%.2f\n" +
                        "📊 Total Transactions: %d\n" +
                        "📋 Status: %s\n" +
                        "📉 Avg Daily Impact: $%.2f\n\n" +
                        "Generated at: %s",
                period.toUpperCase(),
                summary.getStartDate().format(DATE_FORMATTER),
                summary.getEndDate().format(DATE_FORMATTER),
                summary.getTotalCredit(),
                summary.getTotalDebit(),
                summary.getBalance(),
                summary.getTotalTransactions(),
                status,
                avgDailyBalance,
                LocalDateTime.now().format(FORMATTER)
        );
    }

    /**
     * Private helper method to encapsulate the WhatsApp message sending logic.
     * This avoids code duplication.
     *
     * @param messageContent The specific text message to send.
     * @param context        A string describing the context of the message (for logging).
     */
    private void sendWhatsAppMessage(String messageContent, String context) {
        System.out.println("AlertScheduler: Sending WhatsApp message (" + context + ") to " + recipientPhoneNumber);

        // Construct the JSON payload for a text message
        String jsonPayload = String.format(
                "{ \"messaging_product\": \"whatsapp\", " +
                        "\"recipient_type\": \"individual\", " +
                        "\"to\": \"%s\", " +
                        "\"type\": \"text\", " +
                        "\"text\": { \"body\": \"%s\" } }",
                recipientPhoneNumber,
                messageContent.replace("\"", "\\\"").replace("\n", "\\n") // Escape quotes and newlines
        );

        try {
            // Construct the API URI using the injected accountId
            URI apiUri = new URI("https://graph.facebook.com/v22.0/" + whatsappAccountId + "/messages");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(apiUri)
                    .header("Authorization", "Bearer " + whatsappApiToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpClient http = HttpClient.newHttpClient();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("AlertScheduler: WhatsApp API Response (" + context + "): " + response.body());

        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.err.println("AlertScheduler: Error sending WhatsApp message (" + context + "): " + e.getMessage());
            // In a real application, you might log this error to a file,
            // send an alert, or implement retry logic.
        } finally {
            alertRepository.insert(new AlertModel(messageContent.replace("\"", "\\\"").replace("\n", "\\n"), Instant.now()));
        }
    }
    /**
     * Inner class to hold transaction summary data
     */
    private static class TransactionSummary {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final BigDecimal totalCredit;
        private final BigDecimal totalDebit;
        private final BigDecimal balance;
        private final int totalTransactions;

        public TransactionSummary(LocalDate startDate, LocalDate endDate, BigDecimal totalCredit,
                                  BigDecimal totalDebit, BigDecimal balance, int totalTransactions) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalCredit = totalCredit;
            this.totalDebit = totalDebit;
            this.balance = balance;
            this.totalTransactions = totalTransactions;
        }

        public boolean isLoss() {
            return balance.compareTo(BigDecimal.ZERO) < 0;
        }

        public boolean isProfit() {
            return balance.compareTo(BigDecimal.ZERO) > 0;
        }

        public BigDecimal getLossAmount() {
            return isLoss() ? balance : BigDecimal.ZERO;
        }

        // Getters
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public BigDecimal getTotalCredit() { return totalCredit; }
        public BigDecimal getTotalDebit() { return totalDebit; }
        public BigDecimal getBalance() { return balance; }
        public int getTotalTransactions() { return totalTransactions; }
    }
}