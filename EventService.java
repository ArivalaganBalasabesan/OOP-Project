package com.eventmanagement.service;

import com.eventmanagement.model.Event;
import com.eventmanagement.util.MergeSortUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * Service class for managing events
 * Implements business logic for event CRUD operations
 */
@Service
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final FileService fileService;
    @Value("${event.file.path:events.txt}")
    private String EVENT_FILE;

    @Autowired
    public EventService(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Create a new event and save it to file
     */
    public Event createEvent(Event event) throws IOException {
        logger.info("Creating new event: {}", event.getName());
        if (event == null || event.getId() == null || event.getName() == null) {
            logger.error("Invalid event data: {}", event);
            throw new IllegalArgumentException("Event data is invalid");
        }
        List<Event> events = getAllEvents();
        logger.debug("Existing events before adding new event: {}", events);
        events.add(event);
        logger.debug("Events after adding new event: {}", events);
        saveEvents(events);
        logger.info("Event created successfully: {}", event.getId());
        return event;
    }

    /**
     * Get all events from file
     */
    public List<Event> getAllEvents() throws IOException {
        logger.debug("Reading events from file: {}", EVENT_FILE);
        try {
            List<String> lines = fileService.readFile(EVENT_FILE);
            List<Event> events = new ArrayList<>();
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    try {
                        Event event = Event.fromFileString(line);
                        events.add(event);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Skipping invalid event line due to parsing error: {} - {}", line, e.getMessage());
                    } catch (Exception e) {
                        logger.error("Unexpected error parsing event line: {} - {}", line, e.getMessage());
                    }
                }
            }
            logger.debug("Loaded {} valid events: {}", events.size(), events);
            return events;
        } catch (IOException e) {
            logger.error("Error reading events file: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get event by ID
     */
    public Optional<Event> getEventById(String id) throws IOException {
        logger.debug("Fetching event by ID: {}", id);
        try {
            Optional<Event> event = getAllEvents().stream()
                    .filter(e -> e.getId().equals(id))
                    .findFirst();
            if (event.isPresent()) {
                logger.debug("Event found: {}", event.get().getName());
            } else {
                logger.warn("Event not found with ID: {}", id);
            }
            return event;
        } catch (Exception e) {
            logger.error("Error fetching event by ID: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * Update an existing event
     */
    public Event updateEvent(Event updatedEvent) throws IOException {
        logger.info("Updating event: {}", updatedEvent.getId());
        if (updatedEvent == null || updatedEvent.getId() == null) {
            logger.error("Invalid updated event data: {}", updatedEvent);
            throw new IllegalArgumentException("Updated event data is invalid");
        }
        List<Event> events = getAllEvents();
        logger.debug("Existing events before update: {}", events);
        boolean found = false;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(updatedEvent.getId())) {
                events.set(i, updatedEvent);
                found = true;
                break;
            }
        }
        if (!found) {
            logger.warn("Event not found for update: {}", updatedEvent.getId());
            throw new IllegalArgumentException("Event not found with ID: " + updatedEvent.getId());
        }
        logger.debug("Events after update: {}", events);
        saveEvents(events);
        logger.info("Event updated successfully: {}", updatedEvent.getId());
        return updatedEvent;
    }

    /**
     * Delete an event by ID
     */
    public boolean deleteEvent(String id) throws IOException {
        logger.info("Deleting event with ID: {}", id);
        List<Event> events = getAllEvents();
        logger.debug("Existing events before deletion: {}", events);
        boolean removed = events.removeIf(event -> event.getId().equals(id));
        if (removed) {
            logger.debug("Events after deletion: {}", events);
            saveEvents(events);
            logger.info("Event deleted successfully: {}", id);
        } else {
            logger.warn("Event not found for deletion: {}", id);
        }
        return removed;
    }

    /**
     * Save list of events to file
     */
    private void saveEvents(List<Event> events) throws IOException {
        logger.debug("Saving {} events to file: {}", events.size(), EVENT_FILE);
        List<String> lines = new ArrayList<>();
        for (Event event : events) {
            String eventString = event.toFileString();
            if (eventString == null || eventString.trim().isEmpty()) {
                logger.warn("Skipping invalid event string for event: {}", event.getId());
                continue;
            }
            lines.add(eventString);
        }
        logger.debug("Prepared {} lines to write: {}", lines.size(), lines);
        fileService.writeFile(EVENT_FILE, lines);
        logger.debug("Events saved successfully");
    }

    /**
     * Increment popularity count for an event
     */
    public void incrementEventPopularity(String eventId) throws IOException {
        logger.debug("Incrementing popularity for event: {}", eventId);
        Optional<Event> optionalEvent = getEventById(eventId);
        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            event.incrementPopularity();
            updateEvent(event);
            logger.debug("Popularity incremented to: {}", event.getPopularity());
        } else {
            logger.warn("Event not found for popularity increment: {}", eventId);
        }
    }

    /**
     * Search events by name or description
     */
    public List<Event> searchEvents(String keyword) throws IOException {
        logger.debug("Searching events with keyword: {}", keyword);
        keyword = keyword.toLowerCase();
        String finalKeyword = keyword;
        List<Event> results = getAllEvents().stream()
                .filter(event ->
                        event.getName().toLowerCase().contains(finalKeyword) ||
                                event.getDescription().toLowerCase().contains(finalKeyword) ||
                                event.getVenue().toLowerCase().contains(finalKeyword))
                .collect(Collectors.toList());
        logger.debug("Found {} events matching keyword: {}", results.size(), keyword);
        return results;
    }

    /**
     * Get upcoming events (events that haven't happened yet)
     */
    public List<Event> getUpcomingEvents() throws IOException {
        logger.debug("Fetching upcoming events");
        LocalDateTime now = LocalDateTime.now();
        List<Event> upcoming = getAllEvents().stream()
                .filter(event -> event.getDateTime().isAfter(now))
                .collect(Collectors.toList());
        logger.debug("Found {} upcoming events", upcoming.size());
        return upcoming;
    }

    /**
     * Sort events by date using Merge Sort algorithm
     */
    public List<Event> sortEventsByDate(List<Event> events) {
        logger.debug("Sorting {} events by date", events.size());
        List<Event> sorted = MergeSortUtil.sortByDate(events);
        logger.debug("Events sorted by date");
        return sorted;
    }

    /**
     * Sort events by popularity using Merge Sort algorithm
     */
    public List<Event> sortEventsByPopularity(List<Event> events) {
        logger.debug("Sorting {} events by popularity", events.size());
        List<Event> sorted = MergeSortUtil.sortByPopularity(events);
        logger.debug("Events sorted by popularity");
        return sorted;
    }

    /**
     * Get events by organizer
     */
    public List<Event> getEventsByOrganizer(String organizerId) throws IOException {
        logger.debug("Fetching events for organizer: {}", organizerId);
        List<Event> events = getAllEvents().stream()
                .filter(event -> event.getOrganizer().equals(organizerId))
                .collect(Collectors.toList());
        logger.debug("Found {} events for organizer: {}", events.size(), organizerId);
        return events;
    }
}