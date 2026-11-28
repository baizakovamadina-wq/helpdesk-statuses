package com.example.helpdesk;

import com.example.helpdesk.config.Application;
import com.example.helpdesk.domain.DuplicateTicket;
import com.example.helpdesk.domain.TicketNotFound;
import com.example.helpdesk.handler.TicketController;
import com.example.helpdesk.handler.TicketService;
import com.example.helpdesk.security.SecurityConfig;
import com.example.helpdesk.security.SecurityErrorHandler;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
@ContextConfiguration(classes = Application.class)
@Import({
        SecurityConfig.class,
        SecurityErrorHandler.class
})
class TicketControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TicketService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void createTicketReturns201() throws Exception {

        mvc.perform(post("/api/v1/tickets")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_JUNIOR")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessKey": "TEST-001",
                                  "title": "Network problem"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.businessKey").value("TEST-001"))
                .andExpect(jsonPath("$.title").value("Network problem"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(service).register(
                any(UUID.class),
                eq("TEST-001"),
                eq("Network problem")
        );
    }

    @Test
    void blankTitleReturns400() throws Exception {

        mvc.perform(post("/api/v1/tickets")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_JUNIOR")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessKey": "TEST-002",
                                  "title": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("title")));

        verifyNoInteractions(service);
    }

    @Test
    void missingTicketReturns404() throws Exception {

        UUID id = UUID.randomUUID();

        when(service.findById(id))
                .thenThrow(new TicketNotFound(id));

        mvc.perform(get("/api/v1/tickets/{id}", id)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_JUNIOR")
                        )))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void duplicateTicketReturns409() throws Exception {

        doThrow(new DuplicateTicket("TEST-003"))
                .when(service)
                .register(
                        any(UUID.class),
                        eq("TEST-003"),
                        anyString()
                );

        mvc.perform(post("/api/v1/tickets")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_JUNIOR")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessKey": "TEST-003",
                                  "title": "Duplicate ticket"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE"));
    }

    @Test
    void invalidUuidReturns400() throws Exception {

        mvc.perform(get("/api/v1/tickets/abc")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_JUNIOR")
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message")
                        .value("id must be a UUID"));

        verifyNoInteractions(service);
    }

    @Test
    void correlationIdIsReturned() throws Exception {

        mvc.perform(get("/api/v1/tickets/abc")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_JUNIOR")
                        ))
                        .header(
                                "X-Correlation-Id",
                                "test-correlation-123"
                        ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.correlationId")
                        .value("test-correlation-123"));
    }
}