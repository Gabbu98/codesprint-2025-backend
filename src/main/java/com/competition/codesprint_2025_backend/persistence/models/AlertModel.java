package com.competition.codesprint_2025_backend.persistence.models;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Objects;

@Document(collection = "alerts")
public class AlertModel {
    String message;
    Instant timestamp;

    public AlertModel(String message, Instant timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public AlertModel setMessage(String message) {
        this.message = message;
        return this;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public AlertModel setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AlertModel that = (AlertModel) o;
        return Objects.equals(message, that.message) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, timestamp);
    }

    @Override
    public String toString() {
        return "AlertModel{" +
                "message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
