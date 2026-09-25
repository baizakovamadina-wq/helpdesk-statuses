package com.example.helpdesk;

import com.example.helpdesk.domain.TicketId;
import com.example.helpdesk.domain.TicketPolicy;
import com.example.helpdesk.domain.TicketStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketPolicyTest {

    private final TicketPolicy policy = new TicketPolicy();

    @ParameterizedTest
    @CsvSource({
            "OPEN, IN_PROGRESS",
            "IN_PROGRESS, RESOLVED"
    })
    void allowedMoves(TicketStatus from, TicketStatus to) {
        assertEquals(to, policy.move(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "OPEN, RESOLVED",
            "RESOLVED, OPEN"
    })
    void forbiddenMoves(TicketStatus from, TicketStatus to) {
        assertThrows(
                IllegalStateException.class,
                () -> policy.move(from, to)
        );
    }

    @Test
    void blankIdMustThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TicketId(" ")
        );
    }
}