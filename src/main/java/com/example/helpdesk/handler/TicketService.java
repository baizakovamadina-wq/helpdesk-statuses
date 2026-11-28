package com.example.helpdesk.handler;

import com.example.helpdesk.domain.DuplicateTicket;
import com.example.helpdesk.domain.Rule;
import com.example.helpdesk.domain.TicketStatus;
import com.example.helpdesk.domain.TicketNotFound;
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

    public TicketStatus move(
            TicketStatus from,
            TicketStatus to
    ) {
        rule.check(from, to);
        return to;
    }

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

    @Transactional(readOnly = true)
    public TicketResponse findById(UUID id) {

        TicketEntity ticket = tickets.findById(id)
                .orElseThrow(() -> new TicketNotFound(id));

        return new TicketResponse(
                ticket.getId(),
                ticket.getBusinessKey(),
                ticket.getStatus(),
                ticket.getTitle()
        );
    }

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