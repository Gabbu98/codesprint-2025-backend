package com.competition.codesprint_2025_backend.services;

import com.competition.codesprint_2025_backend.persistence.models.TransactionModel;
import com.competition.codesprint_2025_backend.persistence.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Gets spending percentages by category for pie chart
     * Returns Map<Category, Percentage> - perfect for UI pie charts
     */
    public Map<String, Double> getCategorySpendingPercentagesForPieChart() {
        List<TransactionModel> debitTransactions = transactionRepository.findByType("debit");

        // Group by category and sum amounts
        Map<String, BigDecimal> categoryAmounts = debitTransactions.stream()
                .collect(Collectors.groupingBy(
                        transaction -> transaction.getCategory() != null ? transaction.getCategory() : "other",
                        Collectors.reducing(BigDecimal.ZERO, TransactionModel::getAmount, BigDecimal::add)
                ));

        // Calculate total spending
        BigDecimal totalSpending = categoryAmounts.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSpending.equals(BigDecimal.ZERO)) {
            return new HashMap<>();
        }

        // Convert to percentages as Double for easy frontend consumption
        Map<String, Double> percentages = new HashMap<>();
        categoryAmounts.forEach((category, amount) -> {
            BigDecimal percentage = amount
                    .divide(totalSpending, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            percentages.put(category, percentage.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        });

        return percentages;
    }

    public Map<String, Double> getMonthlySpendingTotals() {
        List<TransactionModel> debitTransactions = transactionRepository.findByType("debit");

        // Group by month (YYYY-MM format) and sum amounts
        Map<String, BigDecimal> monthlyAmounts = debitTransactions.stream()
                .collect(Collectors.groupingBy(
                        transaction -> formatTransactionMonth(transaction.getDate()),
                        Collectors.reducing(BigDecimal.ZERO, TransactionModel::getAmount, BigDecimal::add)
                ));

        // Convert BigDecimal to Double for frontend consumption
        Map<String, Double> monthlyTotals = new HashMap<>();
        monthlyAmounts.forEach((month, amount) -> {
            monthlyTotals.put(month, amount.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        });

        return monthlyTotals;
    }

    public List<TransactionModel> getByType(String type) {
        return this.transactionRepository.findByType(type);
    }

    /**
     * Helper method to format transaction date to YYYY-MM string
     */
    private String formatTransactionMonth(Instant date) {
        return DateTimeFormatter.ofPattern("yyyy-MM")
                .format(date.atOffset(ZoneOffset.UTC));
    }

    /**
     * Gets recent transactions limited by count
     */
    public List<TransactionModel> getRecentTransactions(int limit) {
        return transactionRepository.findAll()
                .stream()
                .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate())) // Sort by date desc
                .limit(limit)
                .collect(Collectors.toList());
    }
}
