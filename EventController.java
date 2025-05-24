package com.eventmanagement.controller;

import com.eventmanagement.model.Event;
import com.eventmanagement.model.User;
import com.eventmanagement.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for event-related operations
 */
@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Get all events
     */
    @GetMapping
    public String getAllEvents(Model model) {
        try {
            List<Event> events = eventService.getAllEvents();
            events = eventService.sortEventsByDate(events);
            model.addAttribute("events", events);
            return "events";
        } catch (IOException e) {
            model.addAttribute("error", "Error loading events: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Get event details
     */
    @GetMapping("/{id}")
    public String getEventDetails(@PathVariable String id, Model model) {
        try {
            Optional<Event> event = eventService.getEventById(id);

            if (event.isPresent()) {
                model.addAttribute("event", event.get());
                return "event-details";
            } else {
                model.addAttribute("error", "Event not found");
                return "error";
            }
        } catch (IOException e) {
            model.addAttribute("error", "Error loading event: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Show create event form
     */
    @GetMapping("/create")
    public String showCreateEventForm(Model model, HttpSession session) {
        // Check if user is logged in and is admin
        User user = (User) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/login";
        }

        model.addAttribute("event", new Event());
        return "create-event";
    }

    /**
     * Create new event
     */
    @PostMapping("/create")
    public String createEvent(
            @RequestParam String name,
            @RequestParam String venue,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) String time,
            @RequestParam int capacity,
            @RequestParam double ticketPrice,
            @RequestParam String imageUrl,
            @RequestParam String description,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            System.out.println("User not logged in, redirecting to login");
            return "redirect:/login";
        }
        System.out.println("User logged in: " + user.getId() + ", isAdmin: " + user.isAdmin());

        try {
            System.out.println("Creating event with name: " + name + ", date: " + date + ", time: " + time);
            LocalDateTime dateTime = LocalDateTime.parse(date + "T" + time);
            System.out.println("Parsed dateTime: " + dateTime);

            Event newEvent = new Event();
            newEvent.setName(name);
            newEvent.setDescription(description);
            newEvent.setVenue(venue);
            newEvent.setDateTime(dateTime);
            newEvent.setCapacity(capacity);
            newEvent.setTicketPrice(ticketPrice);
            newEvent.setImageUrl(imageUrl);
            newEvent.setOrganizer(user.getId());
            System.out.println("New event created: " + newEvent.toFileString());

            eventService.createEvent(newEvent);
            redirectAttributes.addFlashAttribute("message", "Event created successfully");
            System.out.println("Event created successfully: " + newEvent.getId() + ", wrote to events.txt");
            return "redirect:/events/my-events";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error creating event: " + e.getMessage());
            System.out.println("IOException in createEvent: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/events/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error creating event: " + e.getMessage());
            System.out.println("Unexpected error in createEvent: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/events/create";
        }
    }

    /**
     * Show edit event form
     */
    @GetMapping("/{id}/edit")
    public String showEditEventForm(@PathVariable String id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        System.out.println("Session user: " + (user != null ? user.toString() : "null")); // Debug
        if (user == null) {
            System.out.println("User not logged in, redirecting to login");
            return "redirect:/login";
        }
        try {
            Optional<Event> event = eventService.getEventById(id);
            if (event.isPresent()) {
                model.addAttribute("event", event.get());
                System.out.println("Navigating to edit form for event ID: " + id + ", user: " + user.getId() + ", isAdmin: " + user.isAdmin());
                return "edit-event";
            } else {
                // Instead of error page, redirect with a message
                System.out.println("Event not found for ID: " + id);
                return "redirect:/events/my-events?error=Event+not+found+with+ID+" + id;
            }
        } catch (IOException e) {
            // Log the exception and redirect instead of showing error page
            System.out.println("IOException in showEditEventForm: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/events/my-events?error=Error+loading+event:+IO+exception";
        }
    }
    /**
     * Update event
     */
    @PostMapping("/{id}")
    public String updateEvent(
            @PathVariable String id,
            @RequestParam String name,
            @RequestParam String venue,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) String time,
            @RequestParam int capacity,
            @RequestParam double ticketPrice,
            @RequestParam String imageUrl,
            @RequestParam String organizer,
            @RequestParam String description,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            Optional<Event> optionalEvent = eventService.getEventById(id);
            if (optionalEvent.isPresent()) {
                Event existingEvent = optionalEvent.get();
                if (!user.isAdmin() && !existingEvent.getOrganizer().equals(user.getId())) {
                    redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this event");
                    System.out.println("Unauthorized update attempt for event ID: " + id);
                    return "redirect:/events/" + id;
                }
                LocalDateTime dateTime = LocalDateTime.parse(date + "T" + time);
                existingEvent.setName(name);
                existingEvent.setDescription(description);
                existingEvent.setVenue(venue);
                existingEvent.setDateTime(dateTime);
                existingEvent.setCapacity(capacity);
                existingEvent.setTicketPrice(ticketPrice);
                existingEvent.setImageUrl(imageUrl);
                existingEvent.setOrganizer(organizer);

                eventService.updateEvent(existingEvent);
                redirectAttributes.addFlashAttribute("message", "Event updated successfully");
                System.out.println("Event updated successfully for ID: " + id + ", wrote to events.txt");
            } else {
                redirectAttributes.addFlashAttribute("error", "Event not found");
                System.out.println("Event not found for update: " + id);
            }
            return "redirect:/events/" + id;
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error updating event: " + e.getMessage());
            System.out.println("IOException in updateEvent: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/events/" + id + "/edit";
        }
    }
    /**
     * Delete event
     */
    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in and is admin
        User user = (User) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/login";
        }

        try {
            boolean deleted = eventService.deleteEvent(id);

            if (deleted) {
                redirectAttributes.addFlashAttribute("message", "Event deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Event not found");
            }

            return "redirect:/events";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting event: " + e.getMessage());
            return "redirect:/events";
        }
    }

    /**
     * Search events
     */
    @GetMapping("/search")
    public String searchEvents(@RequestParam String keyword, Model model) {
        try {
            List<Event> events = eventService.searchEvents(keyword);
            events = eventService.sortEventsByDate(events);

            model.addAttribute("events", events);
            model.addAttribute("keyword", keyword);
            return "events";
        } catch (IOException e) {
            model.addAttribute("error", "Error searching events: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Show my events (for event organizers)
     */
    @GetMapping("/my-events")
    public String showMyEvents(Model model, HttpSession session) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            List<Event> allEvents;
            if (user.isAdmin()) {
                // Admin sees all events
                allEvents = eventService.getAllEvents();
            } else {
                // Regular user sees only their events
                allEvents = eventService.getEventsByOrganizer(user.getId());
            }

            // Separate upcoming events based on current date
            LocalDateTime now = LocalDateTime.now(); // Current date: May 23, 2025, 01:27 PM +0530
            List<Event> upcomingEvents = allEvents.stream()
                    .filter(event -> event.getDateTime().isAfter(now))
                    .collect(Collectors.toList());

            // Sort events by date
            upcomingEvents = eventService.sortEventsByDate(upcomingEvents);
            allEvents = eventService.sortEventsByDate(allEvents); // Sort all events for Past Events tab

            // Add both lists to the model
            model.addAttribute("upcomingEvents", upcomingEvents);
            model.addAttribute("allEvents", allEvents); // Pass all events (or organizer's events) for Past Events tab

            return "my-events";
        } catch (IOException e) {
            model.addAttribute("error", "Error loading your events: " + e.getMessage());
            return "error";
        }
    }
}