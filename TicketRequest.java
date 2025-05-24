package com.eventmanagement.model;

import java.time.LocalDateTime;

/**
 * Represents a ticket booking request in the queue.
 * Part of the Queue data structure implementation.
 */
public class TicketRequest {
    private String userId;
    private String eventId;
    private int quantity;
    private LocalDateTime requestTime;

    public TicketRequest(String userId, String eventId, int quantity) {
        this.userId = userId;
        this.eventId = eventId;
        this.quantity = quantity;
        this.requestTime = LocalDateTime.now();
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getEventId() {
        return eventId;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    @Override
    public String toString() {
        return "TicketRequest{" +
                "userId='" + userId + '\'' +
                ", eventId='" + eventId + '\'' +
                ", quantity=" + quantity +
                ", requestTime=" + requestTime +
                '}';
    }
}