
package com.example.helpdesk;

import com.example.helpdesk.config.Application;
import com.example.helpdesk.domain.DuplicateTicket;
import com.example.helpdesk.handler.TicketService;
import com.example.helpdesk.persistence.TicketJdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
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

    @BeforeEach
    void setup() throws Exception {
        jdbc.execute("DROP TABLE IF EXISTS ticket");

        String schema = Files.readString(
                Path.of("src/main/resources/db/schema.sql")
        );

        jdbc.execute(schema);
    }

    @Test
    void secondStatementRollsBack() {
        assertThrows(
                DuplicateTicket.class,
                () -> service.insertTwice("HD-1042")
        );

        assertEquals(0, tickets.count("HD-1042"));
    }

    @Test
    void secondRequestKeepsTheFirst() {
        service.register(
                UUID.randomUUID(),
                "HD-1042",
                "Printer problem"
        );

        assertThrows(
                DuplicateTicket.class,
                () -> service.register(
                        UUID.randomUUID(),
                        "HD-1042",
                        "Another printer problem"
                )
        );

        assertEquals(1, tickets.count("HD-1042"));
    }
}
