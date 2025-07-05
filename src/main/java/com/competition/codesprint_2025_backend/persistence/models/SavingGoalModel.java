package com.competition.codesprint_2025_backend.persistence.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Objects;

@Document(collection = "savings_goals")
public class SavingGoalModel {
    @Id
    private String id;
    @Indexed
    private String userId;
    private String name;
    private BigDecimal target;
    private BigDecimal saved;
    private BigDecimal remainder;

    public SavingGoalModel(String name, BigDecimal target, BigDecimal saved, BigDecimal remainder) {
        this.name = name;
        this.target = target;
        this.saved = saved;
        this.remainder = remainder;
    }

    public String getId() {
        return id;
    }

    public SavingGoalModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public SavingGoalModel setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getName() {
        return name;
    }

    public SavingGoalModel setName(String name) {
        this.name = name;
        return this;
    }

    public BigDecimal getTarget() {
        return target;
    }

    public SavingGoalModel setTarget(BigDecimal target) {
        this.target = target;
        return this;
    }

    public BigDecimal getSaved() {
        return saved;
    }

    public SavingGoalModel setSaved(BigDecimal saved) {
        this.saved = saved;
        return this;
    }

    public BigDecimal getRemainder() {
        return remainder;
    }

    public SavingGoalModel setRemainder(BigDecimal remainder) {
        this.remainder = remainder;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SavingGoalModel that = (SavingGoalModel) o;
        return Objects.equals(id, that.id) && Objects.equals(userId, that.userId) && Objects.equals(name, that.name) && Objects.equals(target, that.target) && Objects.equals(saved, that.saved) && Objects.equals(remainder, that.remainder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, name, target, saved, remainder);
    }

    @Override
    public String toString() {
        return "SavingGoalModel{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", target=" + target +
                ", saved=" + saved +
                ", remainder=" + remainder +
                '}';
    }
}
