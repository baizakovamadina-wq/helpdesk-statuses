package com.example.helpdesk;

import com.example.helpdesk.config.Application;
import com.example.helpdesk.domain.DuplicateTicket;
import com.example.helpdesk.handler.TicketService;
import com.example.helpdesk.persistence.TicketJdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
class TicketJdbcTest {

    @Autowired
    private TicketService service;

    @Autowired
    private TicketJdbc tickets;

    @Autowired
    private JdbcTemplate jdbc;

    private String businessKey;

    @BeforeEach
    void setup() {
        businessKey = "TEST-" + UUID.randomUUID()
                .toString().substring(0, 8);
    }

    @AfterEach
    void cleanup() {
        jdbc.update(
                "DELETE FROM ticket WHERE business_key = ?",
                businessKey
        );
    }

    @Test
    void secondStatementRollsBack() {

        assertThrows(
                DuplicateTicket.class,
                () -> service.insertTwice(businessKey)
        );

        assertEquals(0, tickets.count(businessKey));
    }

    @Test
    void secondRequestKeepsTheFirst() {

        service.register(
                UUID.randomUUID(),
                businessKey,
                "Printer problem"
        );

        assertThrows(
                DuplicateTicket.class,
                () -> service.register(
                        UUID.randomUUID(),
                        businessKey,
                        "Another printer problem"
                )
        );

        assertEquals(1, tickets.count(businessKey));
    }
}