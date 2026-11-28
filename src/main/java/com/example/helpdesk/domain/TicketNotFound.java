package com.example.helpdesk.domain;

import java.util.UUID;

public class TicketNotFound extends RuntimeException {

    public TicketNotFound(UUID id) {
        super("Ticket not found: " + id);
    }
}