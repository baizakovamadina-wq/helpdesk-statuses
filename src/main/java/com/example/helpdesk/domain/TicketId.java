package com.example.helpdesk.domain;

public record TicketId(String value) {

    public TicketId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Ticket ID must not be blank");
        }
    }
}