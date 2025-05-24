package com.eventmanagement.controller;

import com.eventmanagement.model.Event;
import com.eventmanagement.model.Ticket;
import com.eventmanagement.model.User;
import com.eventmanagement.service.EventService;
import com.eventmanagement.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final EventService eventService;

    @Autowired
    public TicketController(TicketService ticketService, EventService eventService) {
        this.ticketService = ticketService;
        this.eventService = eventService;
    }

    @GetMapping
    public String showUserTickets(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            List<Ticket> tickets = ticketService.getTicketsByUser(user.getId());
            Map<String, String> ticketEventMap = new HashMap<>();
            for (Ticket ticket : tickets) {
                Optional<Event> event = eventService.getEventById(ticket.getEventId());
                event.ifPresent(value -> ticketEventMap.put(ticket.getEventId(), value.getName()));
                if (!event.isPresent()) {
                    ticketEventMap.put(ticket.getEventId(), "Event Not Found");
                }
            }
            model.addAttribute("tickets", tickets);
            model.addAttribute("ticketEventMap", ticketEventMap);
            return "tickets";
        } catch (IOException e) {
            model.addAttribute("error", "Error loading tickets: " + e.getMessage());
            model.addAttribute("tickets", java.util.Collections.emptyList());
            return "tickets";
        }
    }

    @GetMapping("/{id}")
    public String showTicketDetails(@PathVariable String id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            Optional<Ticket> ticket = ticketService.getTicketById(id);
            if (ticket.isPresent() && (ticket.get().getUserId().equals(user.getId()) || user.isAdmin())) {
                model.addAttribute("ticket", ticket.get());
                Optional<Event> event = eventService.getEventById(ticket.get().getEventId());
                event.ifPresent(value -> model.addAttribute("event", value));
                return "ticket-details";
            } else {
                model.addAttribute("error", "Ticket not found or unauthorized");
                return "error";
            }
        } catch (IOException e) {
            model.addAttribute("error", "Error loading ticket: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/book/{eventId}")
    public String showBookingForm(@PathVariable String eventId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?redirect=/tickets/book/" + eventId;
        }
        try {
            Optional<Event> event = eventService.getEventById(eventId);
            if (event.isPresent()) {
                model.addAttribute("event", event.get());
                return "booking";
            } else {
                model.addAttribute("error", "Event not found");
                return "error";
            }
        } catch (IOException e) {
            model.addAttribute("error", "Error loading event: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/book/{eventId}")
    public String bookTicket(
            @PathVariable String eventId,
            @RequestParam int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            Optional<Event> event = eventService.getEventById(eventId);
            if (!event.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Event not found");
                return "redirect:/events";
            }
            ticketService.addTicketRequest(user.getId(), eventId, quantity);
            Ticket ticket = ticketService.processNextTicketRequest();
            if (ticket == null) {
                redirectAttributes.addFlashAttribute("error", "Failed to process ticket request");
                return "redirect:/tickets/book/" + eventId;
            }
            redirectAttributes.addFlashAttribute("message", "Ticket booked successfully");
            return "redirect:/tickets/" + ticket.getId();
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error booking ticket: " + e.getMessage());
            return "redirect:/tickets/book/" + eventId;
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable String id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            Optional<Ticket> ticket = ticketService.getTicketById(id);
            if (ticket.isPresent() && ticket.get().getUserId().equals(user.getId())) {
                model.addAttribute("ticket", ticket.get());
                return "ticket-edit";
            } else {
                model.addAttribute("error", "Ticket not found or unauthorized");
                return "error";
            }
        } catch (IOException e) {
            model.addAttribute("error", "Error loading ticket: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateTicket(
            @PathVariable String id,
            @RequestParam int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            Optional<Ticket> ticket = ticketService.getTicketById(id);
            if (ticket.isPresent() && ticket.get().getUserId().equals(user.getId())) {
                ticketService.updateTicket(id, quantity);
                redirectAttributes.addFlashAttribute("message", "Ticket updated successfully");
                return "redirect:/tickets/" + id;
            } else {
                redirectAttributes.addFlashAttribute("error", "Ticket not found or unauthorized");
                return "redirect:/tickets";
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error updating ticket: " + e.getMessage());
            return "redirect:/tickets/" + id + "/edit";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/tickets/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelTicket(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        try {
            Optional<Ticket> ticket = ticketService.getTicketById(id);
            if (ticket.isPresent() && (ticket.get().getUserId().equals(user.getId()) || user.isAdmin())) {
                boolean cancelled = ticketService.cancelTicket(id);
                if (cancelled) {
                    redirectAttributes.addFlashAttribute("message", "Ticket cancelled successfully");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Failed to cancel ticket");
                }
                return "redirect:/tickets";
            } else {
                redirectAttributes.addFlashAttribute("error", "Ticket not found or unauthorized");
                return "redirect:/tickets";
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling ticket: " + e.getMessage());
            return "redirect:/tickets";
        }
    }
}