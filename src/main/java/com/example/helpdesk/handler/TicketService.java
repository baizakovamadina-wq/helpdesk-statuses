
package com.example.helpdesk.handler;

import com.example.helpdesk.domain.DuplicateTicket;
import com.example.helpdesk.domain.Rule;
import com.example.helpdesk.domain.TicketStatus;
import com.example.helpdesk.persistence.TicketJdbc;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TicketService {

    private final Rule rule;
    private final TicketJdbc tickets;

    public TicketService(Rule rule, TicketJdbc tickets) {
        this.rule = rule;
        this.tickets = tickets;
    }

    public TicketStatus move(TicketStatus from, TicketStatus to) {
        rule.check(from, to);
        return to;
    }

    @Transactional
    public void register(UUID id, String businessKey, String title) {
        try {
            tickets.insert(id, businessKey, "OPEN", title);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateTicket(businessKey);
        }
    }

    @Transactional
    public void insertTwice(String businessKey) {
        try {
            tickets.insert(
                    UUID.randomUUID(),
                    businessKey,
                    "OPEN",
                    "First ticket"
            );

            tickets.insert(
                    UUID.randomUUID(),
                    businessKey,
                    "OPEN",
                    "Second ticket"
            );

        } catch (DuplicateKeyException ex) {
            throw new DuplicateTicket(businessKey);
        }
    }
}
