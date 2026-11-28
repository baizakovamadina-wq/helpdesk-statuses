package com.example.helpdesk.handler;

public record ApiError(
        String code,
        String message,
        String correlationId
) {
}