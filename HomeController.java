package com.eventmanagement.controller;

import com.eventmanagement.model.Event;
import com.eventmanagement.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.List;

/**
 * Controller for home and generic pages
 */
@Controller
public class HomeController {

    private final EventService eventService;

    @Autowired
    public HomeController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Home page
     */
    @GetMapping("/")
    public String homePage(Model model) {
        try {
            System.out.println("Homepage requested - loading events from files");

            // Get all events first to check if we can access the files
            List<Event> allEvents = eventService.getAllEvents();
            System.out.println("Loaded " + allEvents.size() + " events from file");

            // Get upcoming events
            List<Event> upcomingEvents = eventService.getUpcomingEvents();
            System.out.println("Filtered " + upcomingEvents.size() + " upcoming events");

            // Sort by date
            upcomingEvents = eventService.sortEventsByDate(upcomingEvents);

            // Limit to 6 events for the homepage
            if (upcomingEvents.size() > 6) {
                upcomingEvents = upcomingEvents.subList(0, 6);
            }

            // Get popular events
            List<Event> popularEvents = eventService.sortEventsByPopularity(allEvents);

            // Limit to 6 popular events
            if (popularEvents.size() > 6) {
                popularEvents = popularEvents.subList(0, 6);
            }

            model.addAttribute("upcomingEvents", upcomingEvents);
            model.addAttribute("popularEvents", popularEvents);
            model.addAttribute("featuredEvents", popularEvents); // Add featured events for the homepage

            System.out.println("Returning index.html template");
            return "index";
        } catch (Exception e) {
            System.err.println("Error in homepage: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading events: " + e.getMessage());
            return "error";
        }
    }

    /**
     * About page
     */
    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }

    /**
     * Error page
     */
    @GetMapping("/error")
    public String errorPage() {
        return "error";
    }
}