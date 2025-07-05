package com.competition.codesprint_2025_backend.mappers;

import com.competition.codesprint_2025_backend.controllers.responses.TransactionCategoryResponse;
import com.competition.codesprint_2025_backend.persistence.models.TransactionModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class TransactionsMapper {

    public static List<TransactionCategoryResponse> mapTransactionsMapToTransactionCategoryResponse(Map<String, Double> categoriesMap) {
        return categoriesMap.entrySet().stream().map(entry -> new TransactionCategoryResponse(entry.getKey(), BigDecimal.valueOf(entry.getValue()))).toList();
    }

}
