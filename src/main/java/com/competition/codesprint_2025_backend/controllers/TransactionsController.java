package com.competition.codesprint_2025_backend.controllers;

import com.competition.codesprint_2025_backend.controllers.responses.TransactionCategoryResponse;
import com.competition.codesprint_2025_backend.mappers.TransactionsMapper;
import com.competition.codesprint_2025_backend.persistence.models.TransactionModel;
import com.competition.codesprint_2025_backend.services.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v0/transactions")
public class TransactionsController {

    private TransactionService transactionService;

    public TransactionsController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionModel>> getTransactions(){
        return ResponseEntity.ok(transactionService.getRecentTransactions(10));
    }

    @GetMapping("/spending-percentages")
    public ResponseEntity<List<TransactionCategoryResponse>> getSpendingPercentages(){
        return ResponseEntity.ok(TransactionsMapper.mapTransactionsMapToTransactionCategoryResponse(this.transactionService.getCategorySpendingPercentagesForPieChart()));
    }

    @GetMapping("/trends")
    public ResponseEntity<List<TransactionCategoryResponse>> getTrends() {
        return ResponseEntity.ok(TransactionsMapper.mapTransactionsMapToTransactionCategoryResponse(this.transactionService.getMonthlySpendingTotals()));
    }
}
