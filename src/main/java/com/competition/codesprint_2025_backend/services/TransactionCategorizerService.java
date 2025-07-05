package com.competition.codesprint_2025_backend.services;

import com.competition.codesprint_2025_backend.persistence.models.TransactionModel;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TransactionCategorizerService {

    // Category constants
    public static final String FOOD_DELIVERY = "food_delivery";
    public static final String GROCERIES = "groceries";
    public static final String RESTAURANTS = "restaurants";
    public static final String TRANSPORT = "transport";
    public static final String SUBSCRIPTIONS = "subscriptions";
    public static final String SHOPPING = "shopping";
    public static final String ENTERTAINMENT = "entertainment";
    public static final String UTILITIES = "utilities";
    public static final String RENT_HOUSING = "rent_housing";
    public static final String BANKING_FEES = "banking_fees";
    public static final String INCOME = "income";
    public static final String REFUND = "refund";
    public static final String OTHER = "other";

    // Updated categorization rules based on your actual data
    private static final CategoryRule[] CATEGORY_RULES = {
            // Food Delivery - DPZ (Domino's Pizza), Domino's Pizza
            new CategoryRule(FOOD_DELIVERY, Arrays.asList(
                    "dpz mt", "domino's pizza", "domino pizza", "pizza online", "pizza.com"
            )),

            // Groceries - LIDL, Local Deli
            new CategoryRule(GROCERIES, Arrays.asList(
                    "lidl", "local deli", "localdeli", "deli"
            )),

            // Restaurants/Fast Food - McDonald's, Starbucks
            new CategoryRule(RESTAURANTS, Arrays.asList(
                    "mcdonald's", "mcdonald", "starbucks", "tkt*starbucks"
            )),

            // Subscriptions - Netflix, Spotify
            new CategoryRule(SUBSCRIPTIONS, Arrays.asList(
                    "netflix", "spotify"
            )),

            // Shopping - Amazon, Zara, Tech Store, Bookstore, Game Store
            new CategoryRule(SHOPPING, Arrays.asList(
                    "bookstore.com", "bookstore", "zara.com", "zara", "tech store", "techstore",
                    "game store", "gamestore", "amzn mktp", "amazon", "google *bookstore",
                    "google *zara", "tkt*bookstore", "tkt*gamestore", "tkt*techstore",
                    "tkt*amazon", "google *gme"
            )),

            // Entertainment - Cinema, Concert tickets
            new CategoryRule(ENTERTAINMENT, Arrays.asList(
                    "cinema tickets", "cinematicket", "concert", "tkt*concert",
                    "google *concert", "google *cinematicket"
            )),

            // Transport - Parking
            new CategoryRule(TRANSPORT, Arrays.asList(
                    "parking garage", "publicparkin", "public parking", "parkinggar",
                    "google *publicparkin", "google *parkinggarag", "tkt*parkinggar"
            )),

            // Rent/Housing - Monthly rent
            new CategoryRule(RENT_HOUSING, Arrays.asList(
                    "monthlyren", "monthly rent", "rent"
            )),

            // Banking/Fees - FX fees, conversion fees
            new CategoryRule(BANKING_FEES, Arrays.asList(
                    "usd fx fee", "fx fee", "conv to eur", "malta post ltd"
            )),

            // Income - Payroll
            new CategoryRule(INCOME, Arrays.asList(
                    "payroll", "salary"
            )),

            // Refunds
            new CategoryRule(REFUND, Arrays.asList(
                    "refund"
            )),

            // Utilities - Could include Malta Post for postal services
            new CategoryRule(UTILITIES, Arrays.asList(
                    "malta post", "revolut *p2p"
            ))
    };

    /**
     * Categorizes a transaction based on its description
     */
    public String categorizeTransaction(TransactionModel transaction) {
        String description = transaction.getDescription().toLowerCase();

        // Special handling for credit transactions
        if ("credit".equalsIgnoreCase(transaction.getType())) {
            // Check if it's income-related
            for (CategoryRule rule : CATEGORY_RULES) {
                if (INCOME.equals(rule.category) && rule.matches(description)) {
                    return INCOME;
                }
            }
            // Check for refunds
            for (CategoryRule rule : CATEGORY_RULES) {
                if (REFUND.equals(rule.category) && rule.matches(description)) {
                    return REFUND;
                }
            }
            // Default for other credits
            return INCOME;
        }

        // Apply categorization rules for debit transactions
        for (CategoryRule rule : CATEGORY_RULES) {
            if (rule.matches(description)) {
                return rule.category;
            }
        }

        return OTHER;
    }

    /**
     * Updates transaction with category (modifies the transaction object)
     */
    public TransactionModel categorizeAndUpdateTransaction(TransactionModel transaction) {
        String category = categorizeTransaction(transaction);
        transaction.setCategory(category);
        return transaction;
    }

    /**
     * Categorizes a list of transactions
     */
    public List<TransactionModel> categorizeTransactions(List<TransactionModel> transactions) {
        return transactions.stream()
                .map(this::categorizeAndUpdateTransaction)
                .toList();
    }

    /**
     * Gets all available categories
     */
    public List<String> getAllCategories() {
        return Arrays.asList(
                FOOD_DELIVERY, GROCERIES, RESTAURANTS, TRANSPORT, SUBSCRIPTIONS,
                SHOPPING, ENTERTAINMENT, UTILITIES, RENT_HOUSING, BANKING_FEES,
                INCOME, REFUND, OTHER
        );
    }

    // Inner class for category rules
    private static class CategoryRule {
        private final String category;
        private final List<Pattern> patterns;

        public CategoryRule(String category, List<String> keywords) {
            this.category = category;
            this.patterns = keywords.stream()
                    .map(keyword -> Pattern.compile(".*" + Pattern.quote(keyword) + ".*", Pattern.CASE_INSENSITIVE))
                    .toList();
        }

        public boolean matches(String description) {
            return patterns.stream().anyMatch(pattern -> pattern.matcher(description).matches());
        }
    }
}
