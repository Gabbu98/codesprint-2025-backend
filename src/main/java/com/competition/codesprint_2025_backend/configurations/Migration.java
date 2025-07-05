package com.competition.codesprint_2025_backend.configurations;

import com.competition.codesprint_2025_backend.persistence.models.TransactionModel;
import com.competition.codesprint_2025_backend.services.TransactionCategorizerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class Migration {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private TransactionCategorizerService transactionCategorizerService;

    /**
     * Reads transactions from a CSV file
     *
     * @param filePath Path to the CSV file
     * @return List of Transaction objects
     */
    public static List<TransactionModel> readTransactionsFromFile(String filePath) {
        List<TransactionModel> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                TransactionModel transaction = parseLineToTransaction(line);
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file: " + filePath, e);
        }

        return transactions;
    }

    /**
     * Parses a CSV line into a Transaction object
     * Format: ,date,description,amount,type,account_number,currency
     */
    private static TransactionModel parseLineToTransaction(String line) {
        try {
            String[] parts = line.split(",");

            if (parts.length < 7) {
                return null; // Skip invalid lines
            }

            // Parse each field (index 0 is the row number, so we start from 1)
            String id = parts[0];
            String dateStr = parts[1];
            String description = parts[2];
            String amountStr = parts[3];
            String type = parts[4];
            String accountNumber = parts[5];
            String currency = parts[6];

            // Convert date to Instant
            LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            Instant date = localDate.atStartOfDay().toInstant(ZoneOffset.UTC);

            // Convert amount to BigDecimal (remove negative sign, we track debit/credit separately)
            BigDecimal amount = new BigDecimal(amountStr.replace("-", ""));

            return new TransactionModel(id, date, description, amount, type, accountNumber, currency,null);

        } catch (Exception e) {
            // Skip problematic lines
            System.err.println("Skipping invalid line: " + line + " - " + e.getMessage());
            return null;
        }
    }

    @EventListener
    public void migrateData(ApplicationReadyEvent ctx) {
        List<TransactionModel> transactionModels = readTransactionsFromFile("src/main/resources/static/codesprint_open_2025_sample_data.csv");
        transactionModels = transactionCategorizerService.categorizeTransactions(transactionModels);
        System.out.println();
    }
}
