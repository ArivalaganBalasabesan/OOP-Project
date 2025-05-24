package com.eventmanagement.service;

import com.eventmanagement.model.Event;
import com.eventmanagement.model.Ticket;
import com.eventmanagement.model.TicketQueue;
import com.eventmanagement.model.TicketRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Value("${ticket.file.path:data/tickets.txt}")
    private String TICKET_FILE;
    private final FileService fileService;
    private final EventService eventService;
    private final TicketQueue ticketQueue;

    @Autowired
    public TicketService(FileService fileService, EventService eventService) {
        this.fileService = fileService;
        this.eventService = eventService;
        this.ticketQueue = new TicketQueue();
    }

    public Ticket bookTicket(String eventId, String userId, int quantity) throws IOException {
        Optional<Event> optionalEvent = eventService.getEventById(eventId);
        if (!optionalEvent.isPresent()) {
            throw new IllegalArgumentException("Event not found");
        }
        Event event = optionalEvent.get();
        double totalPrice = event.getTicketPrice() * quantity;
        Ticket ticket = new Ticket(eventId, userId, quantity, totalPrice);
        List<Ticket> tickets = getAllTickets();
        tickets.add(ticket);
        saveTickets(tickets);
        eventService.incrementEventPopularity(eventId);
        return ticket;
    }

    public List<Ticket> getAllTickets() throws IOException {
        List<String> lines = fileService.readFile(TICKET_FILE);
        List<Ticket> tickets = new ArrayList<>();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                try {
                    tickets.add(Ticket.fromFileString(line));
                } catch (Exception e) {
                    System.err.println("Error parsing ticket line: " + line + " - " + e.getMessage());
                }
            }
        }
        return tickets;
    }

    public Optional<Ticket> getTicketById(String id) throws IOException {
        return getAllTickets().stream()
                .filter(ticket -> ticket.getId().equals(id))
                .findFirst();
    }

    public List<Ticket> getTicketsByUser(String userId) throws IOException {
        return getAllTickets().stream()
                .filter(ticket -> ticket.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<Ticket> getTicketsByEvent(String eventId) throws IOException {
        return getAllTickets().stream()
                .filter(ticket -> ticket.getEventId().equals(eventId))
                .collect(Collectors.toList());
    }

    public Ticket updateTicketStatus(String ticketId, String status) throws IOException {
        Optional<Ticket> optionalTicket = getTicketById(ticketId);
        if (!optionalTicket.isPresent()) {
            throw new IllegalArgumentException("Ticket not found");
        }
        Ticket ticket = optionalTicket.get();
        ticket.setStatus(status);
        List<Ticket> tickets = getAllTickets();
        for (int i = 0; i < tickets.size(); i++) {
            if (tickets.get(i).getId().equals(ticketId)) {
                tickets.set(i, ticket);
                break;
            }
        }
        saveTickets(tickets);
        return ticket;
    }

    public Ticket updateTicket(String ticketId, int quantity) throws IOException {
        Optional<Ticket> optionalTicket = getTicketById(ticketId);
        if (!optionalTicket.isPresent()) {
            throw new IllegalArgumentException("Ticket not found");
        }
        Ticket ticket = optionalTicket.get();
        if (ticket.getStatus().equals("CANCELLED")) {
            throw new IllegalStateException("Cannot update a cancelled ticket");
        }
        Optional<Event> optionalEvent = eventService.getEventById(ticket.getEventId());
        if (!optionalEvent.isPresent()) {
            throw new IllegalArgumentException("Associated event not found");
        }
        Event event = optionalEvent.get();
        ticket.setQuantity(quantity);
        ticket.setTotalPrice(event.getTicketPrice() * quantity);
        List<Ticket> tickets = getAllTickets();
        for (int i = 0; i < tickets.size(); i++) {
            if (tickets.get(i).getId().equals(ticketId)) {
                tickets.set(i, ticket);
                break;
            }
        }
        saveTickets(tickets);
        return ticket;
    }

    public boolean cancelTicket(String ticketId) throws IOException {
        Optional<Ticket> optionalTicket = getTicketById(ticketId);
        if (!optionalTicket.isPresent()) {
            return false;
        }
        return updateTicketStatus(ticketId, "CANCELLED") != null;
    }

    private void saveTickets(List<Ticket> tickets) throws IOException {
        List<String> lines = tickets.stream()
                .map(Ticket::toFileString)
                .collect(Collectors.toList());
        fileService.writeFile(TICKET_FILE, lines);
    }

    public void addTicketRequest(String userId, String eventId, int quantity) {
        TicketRequest request = new TicketRequest(userId, eventId, quantity);
        ticketQueue.addRequest(request);
    }

    public Ticket processNextTicketRequest() throws IOException {
        if (ticketQueue.isEmpty()) {
            return null;
        }
        TicketRequest request = ticketQueue.processNextRequest();
        return bookTicket(request.getEventId(), request.getUserId(), request.getQuantity());
    }

    public List<TicketRequest> getAllPendingRequests() {
        return new ArrayList<>(ticketQueue.getAllRequests());
    }

    public int getPendingRequestCount() {
        return ticketQueue.size();
    }
}