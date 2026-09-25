package com.example.helpdesk;

import com.example.helpdesk.config.Application;
import com.example.helpdesk.domain.TicketStatus;
import com.example.helpdesk.handler.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = Application.class)
class TicketServiceTest {

    @Autowired
    private TicketService ticketService;

    @Test
    void shouldMoveTicketFromOpenToInProgress() {
        TicketStatus result =
                ticketService.move(
                        TicketStatus.OPEN,
                        TicketStatus.IN_PROGRESS
                );

        assertEquals(TicketStatus.IN_PROGRESS, result);
    }

    @Test
    void shouldRejectInvalidTransition() {
        assertThrows(
                IllegalStateException.class,
                () -> ticketService.move(
                        TicketStatus.OPEN,
                        TicketStatus.RESOLVED
                )
        );
    }
}