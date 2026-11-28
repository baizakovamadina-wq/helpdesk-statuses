package com.example.helpdesk.handler;

import com.example.helpdesk.domain.DuplicateTicket;
import com.example.helpdesk.domain.Rule;
import com.example.helpdesk.domain.TicketStatus;
import com.example.helpdesk.domain.TicketNotFound;
import com.example.helpdesk.persistence.TicketEntity;
import com.example.helpdesk.persistence.TicketJpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TicketService {

    private static final Logger log =
            LoggerFactory.getLogger(TicketService.class);

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

    @PreAuthorize("hasRole('SENIOR')")
    @Transactional
    public TicketResponse start(UUID id, String actor) {

        TicketEntity ticket = tickets.findById(id)
                .orElseThrow(() -> new TicketNotFound(id));

        TicketStatus currentStatus =
                TicketStatus.valueOf(ticket.getStatus());

        TicketStatus newStatus = move(
                currentStatus,
                TicketStatus.IN_PROGRESS
        );

        ticket.setStatus(newStatus.name());

        tickets.flush();

        log.info(
                "Ticket {} changed from {} to {} by {}",
                id,
                currentStatus,
                newStatus,
                actor
        );

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