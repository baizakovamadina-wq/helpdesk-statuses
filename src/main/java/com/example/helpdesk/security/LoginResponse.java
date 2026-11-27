package com.example.helpdesk.security;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}