package com.example.helpdesk;

import com.example.helpdesk.config.Application;
import com.example.helpdesk.handler.TicketService;
import com.example.helpdesk.handler.TicketResponse;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("dev")
@Transactional
class ServiceSecurityTest {

    @Autowired
    private TicketService service;

    @Test
    @WithMockUser(username = "junior", roles = "JUNIOR")
    void juniorCannotStartTicketDirectly() {

        UUID id = UUID.randomUUID();

        String businessKey = "SEC-" +
                UUID.randomUUID().toString().substring(0, 8);

        service.register(id, businessKey, "Security test");

        assertThrows(
                AccessDeniedException.class,
                () -> service.start(id, "junior")
        );

        TicketResponse ticket = service.findById(id);

        assertEquals("OPEN", ticket.status());
    }

    @Test
    @WithMockUser(username = "senior", roles = "SENIOR")
    void seniorCanStartTicketDirectly() {

        UUID id = UUID.randomUUID();

        String businessKey = "SEC-" +
                UUID.randomUUID().toString().substring(0, 8);

        service.register(id, businessKey, "Security test");

        TicketResponse result = service.start(id, "senior");

        assertEquals("IN_PROGRESS", result.status());

        TicketResponse saved = service.findById(id);

        assertEquals("IN_PROGRESS", saved.status());
    }
}