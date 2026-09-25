package com.example.helpdesk.handler;

import com.example.helpdesk.domain.Rule;
import com.example.helpdesk.domain.TicketStatus;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

    private final Rule rule;

    public TicketService(Rule rule) {
        this.rule = rule;
    }

    public TicketStatus move(TicketStatus from, TicketStatus to) {
        rule.check(from, to);
        return to;
    }
}