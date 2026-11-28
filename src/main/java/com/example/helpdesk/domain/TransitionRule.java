package com.example.helpdesk.domain;

public class TransitionRule implements Rule {

    @Override
    public void check(TicketStatus from, TicketStatus to) {
        if (from == TicketStatus.OPEN && to == TicketStatus.IN_PROGRESS) {
            return;
        }

        if (from == TicketStatus.IN_PROGRESS && to == TicketStatus.RESOLVED) {
            return;
        }

        throw new IllegalStateException(
                "Transition from " + from + " to " + to + " is not allowed"
        );
    }
}