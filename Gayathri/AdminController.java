package com.eventmanagement.controller;

import com.eventmanagement.model.Event;
import com.eventmanagement.model.Ticket;
import com.eventmanagement.model.TicketRequest;
import com.eventmanagement.model.User;
import com.eventmanagement.service.EventService;
import com.eventmanagement.service.TicketService;
import com.eventmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for admin operations
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final EventService eventService;
    private final TicketService ticketService;

    @Autowired
    public AdminController(UserService userService, EventService eventService, TicketService ticketService) {
        this.userService = userService;
        this.eventService = eventService;
        this.ticketService = ticketService;
    }

    /**
     * Admin dashboard
     */
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        try {
            // Check if user is logged in and is admin
            User user = (User) session.getAttribute("user");
            if (user == null || !user.isAdmin()) {
                return "redirect:/login";
            }

            // Load stats for dashboard
            List<Event> events = eventService.getAllEvents();
            List<User> users = userService.getAllUsers();
            List<Ticket> tickets = ticketService.getAllTickets();

            model.addAttribute("totalEvents", events.size());
            model.addAttribute("totalUsers", users.size());
            model.addAttribute("totalTickets", tickets.size());
            model.addAttribute("pendingRequests", ticketService.getPendingRequestCount());

            // Get recent events
            List<Event> recentEvents = eventService.sortEventsByDate(events);
            if (recentEvents.size() > 5) {
                recentEvents = recentEvents.subList(0, 5);
            }
            model.addAttribute("recentEvents", recentEvents);

            return "admin-dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading admin dashboard: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Manage users
     */
    @GetMapping("/users")
    public String manageUsers(Model model, HttpSession session) {
        try {
            // Check if user is logged in and is admin
            User user = (User) session.getAttribute("user");
            if (user == null || !user.isAdmin()) {
                return "redirect:/login";
            }

            List<User> users = userService.getAllUsers();
            model.addAttribute("users", users);
            return "admin-users";
        } catch (IOException e) {
            model.addAttribute("error", "Error loading users: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Delete user
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            // Check if user is logged in and is admin
            User user = (User) session.getAttribute("user");
            if (user == null || !user.isAdmin()) {
                return "redirect:/login";
            }

            boolean deleted = userService.deleteUser(id);

            if (deleted) {
                redirectAttributes.addFlashAttribute("message", "User deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found");
            }

            return "redirect:/admin/users";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * Toggle admin status
     */
    @PostMapping("/users/{id}/toggle-admin")
    public String toggleAdminStatus(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            // Check if user is logged in and is admin
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null || !currentUser.isAdmin()) {
                return "redirect:/login";
            }

            // Prevent self-demotion
            if (id.equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "You cannot change your own admin status");
                return "redirect:/admin/users";
            }

            Optional<User> optionalUser = userService.getUserById(id);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setAdmin(!user.isAdmin());
                userService.updateUser(user);

                redirectAttributes.addFlashAttribute("message",
                        user.isAdmin() ? "User promoted to admin" : "Admin demoted to regular user");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found");
            }

            return "redirect:/admin/users";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * Manage tickets
     */
    @GetMapping("/tickets")
    public String manageTickets(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.isAdmin()) {
            return "redirect:/login";
        }
        try {
            List<Ticket> allTickets = ticketService.getAllTickets();
            List<Ticket> bookedTickets = allTickets.stream()
                    .filter(ticket -> "CONFIRMED".equals(ticket.getStatus()))
                    .collect(Collectors.toList());
            model.addAttribute("tickets", bookedTickets);

            // Map event IDs to names with error handling
            Map<String, String> ticketEventMap = new HashMap<>();
            for (Ticket ticket : bookedTickets) {
                try {
                    Optional<Event> eventOptional = eventService.getEventById(ticket.getEventId());
                    ticketEventMap.put(ticket.getEventId(), eventOptional.map(Event::getName).orElse("Event Not Found"));
                } catch (IOException e) {
                    ticketEventMap.put(ticket.getEventId(), "Event Not Found");
                    System.err.println("Error fetching event for ticket " + ticket.getId() + ": " + e.getMessage());
                }
            }
            model.addAttribute("ticketEventMap", ticketEventMap);
            return "admin-tickets";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading tickets: " + e.getMessage());
            model.addAttribute("tickets", new ArrayList<>());
            model.addAttribute("ticketEventMap", new HashMap<>());
            return "admin-tickets";
        }
    }

    /**
     * Process next ticket request
     */
    @PostMapping("/tickets/process-next")
    public String processNextTicket(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            // Check if user is logged in and is admin
            User user = (User) session.getAttribute("user");
            if (user == null || !user.isAdmin()) {
                return "redirect:/login";
            }

            Ticket ticket = ticketService.processNextTicketRequest();

            if (ticket != null) {
                redirectAttributes.addFlashAttribute("message", "Ticket request processed successfully");
            } else {
                redirectAttributes.addFlashAttribute("info", "No pending ticket requests");
            }

            return "redirect:/admin/tickets";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error processing ticket request: " + e.getMessage());
            return "redirect:/admin/tickets";
        }
    }

    /**
     * Update ticket status
     */
    @PostMapping("/tickets/{id}/update-status")
    public String updateTicketStatus(
            @PathVariable String id,
            @RequestParam String status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // Check if user is logged in and is admin
            User user = (User) session.getAttribute("user");
            if (user == null || !user.isAdmin()) {
                return "redirect:/login";
            }

            Ticket ticket = ticketService.updateTicketStatus(id, status);

            if (ticket != null) {
                redirectAttributes.addFlashAttribute("message", "Ticket status updated to " + status);
            } else {
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
            }

            return "redirect:/admin/tickets";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error updating ticket status: " + e.getMessage());
            return "redirect:/admin/tickets";
        }
    }
}