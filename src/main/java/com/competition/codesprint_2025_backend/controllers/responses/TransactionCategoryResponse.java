package com.competition.codesprint_2025_backend.controllers.responses;

import java.math.BigDecimal;
import java.util.Objects;

public class TransactionCategoryResponse {
    private String category;  // The key
    private BigDecimal value;  // The value

    private TransactionCategoryResponse() {
    }

    public TransactionCategoryResponse(String category, BigDecimal percentage) {
        this.category = category;
        this.value = percentage;
    }

    public String getCategory() {
        return category;
    }

    public TransactionCategoryResponse setCategory(String category) {
        this.category = category;
        return this;
    }

    public BigDecimal getValue() {
        return value;
    }

    public TransactionCategoryResponse setValue(BigDecimal value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TransactionCategoryResponse that = (TransactionCategoryResponse) o;
        return Objects.equals(category, that.category) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, value);
    }

    @Override
    public String toString() {
        return "TransactionCategoryResponse{" +
                "category='" + category + '\'' +
                ", value=" + value +
                '}';
    }
}
