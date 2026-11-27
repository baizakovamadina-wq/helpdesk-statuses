package com.example.helpdesk.security;

import java.util.List;

public record MeResponse(
        String username,
        List<String> authorities
) {
}