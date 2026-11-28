package com.example.helpdesk;

import com.example.helpdesk.config.Application;
import com.example.helpdesk.handler.TicketController;
import com.example.helpdesk.handler.TicketResponse;
import com.example.helpdesk.handler.TicketService;
import com.example.helpdesk.security.MeController;
import com.example.helpdesk.security.SecurityConfig;
import com.example.helpdesk.security.SecurityErrorHandler;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({
        TicketController.class,
        MeController.class
})
@ContextConfiguration(classes = Application.class)
@Import({
        SecurityConfig.class,
        SecurityErrorHandler.class
})
class SecurityTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TicketService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void requestWithoutTokenReturns401() throws Exception {

        UUID id = UUID.randomUUID();

        mvc.perform(post("/api/v1/tickets/{id}/start", id)
                        .header("X-Correlation-Id", "security-401"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message")
                        .value("valid token required"))
                .andExpect(jsonPath("$.correlationId")
                        .value("security-401"));

        verifyNoInteractions(service);
    }

    @Test
    void juniorCannotStartTicket() throws Exception {

        UUID id = UUID.randomUUID();

        mvc.perform(post("/api/v1/tickets/{id}/start", id)
                        .with(jwt()
                                .jwt(token -> token.subject("junior"))
                                .authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_JUNIOR"
                                        )
                                ))
                        .header("X-Correlation-Id", "security-403"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code")
                        .value("FORBIDDEN"))
                .andExpect(jsonPath("$.message")
                        .value("not allowed for your role"))
                .andExpect(jsonPath("$.correlationId")
                        .value("security-403"));

        verifyNoInteractions(service);
    }

    @Test
    void seniorCanStartTicket() throws Exception {

        UUID id = UUID.randomUUID();

        when(service.start(id, "senior"))
                .thenReturn(new TicketResponse(
                        id,
                        "LAB5-TEST",
                        "IN_PROGRESS",
                        "Network problem"
                ));

        mvc.perform(post("/api/v1/tickets/{id}/start", id)
                        .with(jwt()
                                .jwt(token -> token.subject("senior"))
                                .authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_SENIOR"
                                        )
                                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("IN_PROGRESS"));

        verify(service).start(id, "senior");
    }

    @Test
    void meWithoutTokenReturns401() throws Exception {

        mvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("UNAUTHENTICATED"));
    }

    @Test
    void meReturnsJuniorIdentity() throws Exception {

        mvc.perform(get("/api/me")
                        .with(jwt()
                                .jwt(token -> token.subject("junior"))
                                .authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_JUNIOR"
                                        )
                                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username")
                        .value("junior"))
                .andExpect(jsonPath("$.authorities[0]")
                        .value("ROLE_JUNIOR"));
    }

    @Test
    void meReturnsSeniorIdentity() throws Exception {

        mvc.perform(get("/api/me")
                        .with(jwt()
                                .jwt(token -> token.subject("senior"))
                                .authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_SENIOR"
                                        )
                                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username")
                        .value("senior"))
                .andExpect(jsonPath("$.authorities[0]")
                        .value("ROLE_SENIOR"));
    }
}
