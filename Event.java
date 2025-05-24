package com.eventmanagement.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents an event in the system.
 * This class demonstrates encapsulation by keeping its fields private
 * and providing getters and setters for controlled access.
 */
public class Event implements Serializable {
    private String id;
    private String name;
    private String description;
    private String venue;
    private LocalDateTime dateTime;
    private int capacity;
    private int popularity;
    private double ticketPrice;
    private String imageUrl;
    private String organizer;

    // Default constructor - required for deserialization
    public Event() {
        this.id = UUID.randomUUID().toString();
    }

    // Parameterized constructor
    public Event(String name, String description, String venue, LocalDateTime dateTime,
                 int capacity, double ticketPrice, String imageUrl, String organizer) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.venue = venue;
        this.dateTime = dateTime;
        this.capacity = capacity;
        this.popularity = 0; // Initial popularity
        this.ticketPrice = ticketPrice;
        this.imageUrl = imageUrl;
        this.organizer = organizer;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public void incrementPopularity() {
        this.popularity++;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    // Helper method to format date for display
    public String getFormattedDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter);
    }

    // For file storage - convert to string format
    public String toFileString() {
        return id + "|" + name + "|" + description + "|" + venue + "|" +
                dateTime.toString() + "|" + capacity + "|" + popularity + "|" +
                ticketPrice + "|" + imageUrl + "|" + organizer;
    }

    // For file storage - parse from string format
    public static Event fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 10) {
            throw new IllegalArgumentException("Invalid event data: " + line);
        }

        Event event = new Event();
        event.setId(parts[0]);
        event.setName(parts[1]);
        event.setDescription(parts[2]);
        event.setVenue(parts[3]);
        event.setDateTime(LocalDateTime.parse(parts[4]));
        event.setCapacity(Integer.parseInt(parts[5]));
        event.setPopularity(Integer.parseInt(parts[6]));
        event.setTicketPrice(Double.parseDouble(parts[7]));
        event.setImageUrl(parts[8]);
        event.setOrganizer(parts[9]);

        return event;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", venue='" + venue + '\'' +
                ", dateTime=" + dateTime +
                ", popularity=" + popularity +
                '}';
    }
}