package com.example.helpdesk.domain;

public class HoldRule implements Rule {

    @Override
    public void check(TicketStatus from, TicketStatus to) {
        if (from == TicketStatus.RESOLVED) {
            throw new IllegalStateException(
                    "Resolved ticket cannot be changed"
            );
        }
    }
}