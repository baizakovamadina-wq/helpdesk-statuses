
package com.example.helpdesk.handler;

import com.example.helpdesk.domain.DuplicateTicket;
import com.example.helpdesk.domain.Rule;
import com.example.helpdesk.domain.TicketStatus;
import com.example.helpdesk.persistence.TicketEntity;
import com.example.helpdesk.persistence.TicketJpa;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TicketService {

    private final Rule rule;
    private final TicketJpa tickets;

    public TicketService(Rule rule, TicketJpa tickets) {
        this.rule = rule;
        this.tickets = tickets;
    }

    // Check whether a status transition is allowed
    public TicketStatus move(
            TicketStatus from,
            TicketStatus to
    ) {
        rule.check(from, to);
        return to;
    }

    // Create a new ticket
    @Transactional
    public void register(
            UUID id,
            String businessKey,
            String title
    ) {
        try {
            tickets.insert(
                    createTicket(id, businessKey, title)
            );

            tickets.flush();

        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateTicket(businessKey);
        }
    }

    // Insert two tickets with the same business key
    @Transactional
    public void insertTwice(String businessKey) {
        try {
            tickets.insert(
                    createTicket(
                            UUID.randomUUID(),
                            businessKey,
                            "First ticket"
                    )
            );

            tickets.insert(
                    createTicket(
                            UUID.randomUUID(),
                            businessKey,
                            "Second ticket"
                    )
            );

            tickets.flush();

        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateTicket(businessKey);
        }
    }

    // Convert ticket data into a JPA entity
    private TicketEntity createTicket(
            UUID id,
            String businessKey,
            String title
    ) {
        TicketEntity ticket = new TicketEntity();

        ticket.setId(id);
        ticket.setBusinessKey(businessKey);
        ticket.setStatus(TicketStatus.OPEN.name());
        ticket.setTitle(title);

        return ticket;
    }
}
