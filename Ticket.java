package com.eventmanagement.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents a ticket in the system.
 */
public class Ticket implements Serializable {
    private String id;
    private String eventId;
    private String userId;
    private int quantity;
    private double totalPrice;
    private LocalDateTime bookingDateTime;
    private String status;

    // Default constructor - required for deserialization
    public Ticket() {
        this.id = UUID.randomUUID().toString();
        this.bookingDateTime = LocalDateTime.now();
    }

    // Parameterized constructor
    public Ticket(String eventId, String userId, int quantity, double totalPrice) {
        this.id = UUID.randomUUID().toString();
        this.eventId = eventId;
        this.userId = userId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.bookingDateTime = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getBookingDateTime() {
        return bookingDateTime;
    }

    public void setBookingDateTime(LocalDateTime bookingDateTime) {
        this.bookingDateTime = bookingDateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // For file storage - convert to string format
    public String toFileString() {
        return id + "|" + eventId + "|" + userId + "|" + quantity + "|" +
                totalPrice + "|" + bookingDateTime.toString() + "|" + status;
    }

    // For file storage - parse from string format
    public static Ticket fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 7) {
            throw new IllegalArgumentException("Invalid ticket data: " + line);
        }

        Ticket ticket = new Ticket();
        ticket.setId(parts[0]);
        ticket.setEventId(parts[1]);
        ticket.setUserId(parts[2]);
        ticket.setQuantity(Integer.parseInt(parts[3]));
        ticket.setTotalPrice(Double.parseDouble(parts[4]));
        ticket.setBookingDateTime(LocalDateTime.parse(parts[5]));
        ticket.setStatus(parts[6]);

        return ticket;
    }

    // Helper method for formatting booking time
    public String getFormattedBookingTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return bookingDateTime.format(formatter);
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id='" + id + '\'' +
                ", eventId='" + eventId + '\'' +
                ", userId='" + userId + '\'' +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                ", bookingDateTime=" + bookingDateTime +
                ", status='" + status + '\'' +
                '}';
    }
}