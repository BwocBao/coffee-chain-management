package com.coffeechain.dto.response;

import java.time.LocalDateTime;

public class ExpiryRefreshResponse {
    private String message;
    private LocalDateTime refreshedAt;

    public ExpiryRefreshResponse() {
    }

    public ExpiryRefreshResponse(String message, LocalDateTime refreshedAt) {
        this.message = message;
        this.refreshedAt = refreshedAt;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getRefreshedAt() {
        return refreshedAt;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRefreshedAt(LocalDateTime refreshedAt) {
        this.refreshedAt = refreshedAt;
    }
}