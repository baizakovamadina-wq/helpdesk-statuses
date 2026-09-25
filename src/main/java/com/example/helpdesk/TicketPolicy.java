package com.example.helpdesk;

public class TicketPolicy {

    public TicketStatus move(TicketStatus from, TicketStatus to) {

        if (from == TicketStatus.OPEN
                && to == TicketStatus.IN_PROGRESS) {
            return to;
        }

        if (from == TicketStatus.IN_PROGRESS
                && to == TicketStatus.RESOLVED) {
            return to;
        }

        throw new IllegalStateException(
                "Forbidden status change: " + from + " -> " + to
        );
    }
}