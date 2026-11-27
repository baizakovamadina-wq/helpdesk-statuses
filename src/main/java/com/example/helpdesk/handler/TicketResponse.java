package com.example.helpdesk.handler;

import java.util.UUID;

public record TicketResponse(
        UUID id,
        String businessKey,
        String status,
        String title
) {
}