package com.competition.codesprint_2025_backend.persistence.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Document(collection = "transactions")
public class TransactionModel {

    @Id
    private String id;

    private Instant date;

    private String description;

    private BigDecimal amount;

    @Indexed
    private String type;

    private String accountNumber;

    private String currency;

    private String category;

    public TransactionModel(String id, Instant date, String description, BigDecimal amount, String type, String accountNumber, String currency, String category) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.accountNumber = accountNumber;
        this.currency = currency;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public TransactionModel setId(String id) {
        this.id = id;
        return this;
    }

    public Instant getDate() {
        return date;
    }

    public TransactionModel setDate(Instant date) {
        this.date = date;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public TransactionModel setDescription(String description) {
        this.description = description;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionModel setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public String getType() {
        return type;
    }

    public TransactionModel setType(String type) {
        this.type = type;
        return this;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public TransactionModel setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public TransactionModel setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public TransactionModel setCategory(String category) {
        this.category = category;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TransactionModel that = (TransactionModel) o;
        return Objects.equals(id, that.id) && Objects.equals(date, that.date) && Objects.equals(description, that.description) && Objects.equals(amount, that.amount) && Objects.equals(type, that.type) && Objects.equals(accountNumber, that.accountNumber) && Objects.equals(currency, that.currency) && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, description, amount, type, accountNumber, currency, category);
    }

    @Override
    public String toString() {
        return "TransactionModel{" +
                "id='" + id + '\'' +
                ", date=" + date +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", currency='" + currency + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
