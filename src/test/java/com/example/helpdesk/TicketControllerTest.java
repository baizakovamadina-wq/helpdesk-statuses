package com.example.helpdesk;

import com.example.helpdesk.config.Application;
import com.example.helpdesk.domain.DuplicateTicket;
import com.example.helpdesk.domain.TicketNotFound;
import com.example.helpdesk.handler.TicketController;
import com.example.helpdesk.handler.TicketService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
@ContextConfiguration(classes = Application.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TicketService service;

    @Test
    void shouldCreateTicket() throws Exception {

        mvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "businessKey": "TEST-001",
                                  "title": "Network problem"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.businessKey").value("TEST-001"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.title").value("Network problem"))
                .andExpect(jsonPath("$.id").exists());

        verify(service).register(
                any(UUID.class),
                eq("TEST-001"),
                eq("Network problem")
        );
    }

    @Test
    void shouldRejectBlankTitle() throws Exception {

        mvc.perform(post("/api/v1/tickets")
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
    void shouldReturnNotFound() throws Exception {

        UUID id = UUID.randomUUID();

        when(service.findById(id))
                .thenThrow(new TicketNotFound(id));

        mvc.perform(get("/api/v1/tickets/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void shouldRejectDuplicateTicket() throws Exception {

        doThrow(new DuplicateTicket("TEST-003"))
                .when(service)
                .register(
                        any(UUID.class),
                        eq("TEST-003"),
                        eq("Duplicate ticket")
                );

        mvc.perform(post("/api/v1/tickets")
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
    void shouldRejectInvalidUuid() throws Exception {

        mvc.perform(get("/api/v1/tickets/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message")
                        .value("id must be a UUID"));

        verifyNoInteractions(service);
    }

    @Test
    void shouldEchoCorrelationId() throws Exception {

        mvc.perform(get("/api/v1/tickets/abc")
                        .header("X-Correlation-Id", "test-correlation-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.correlationId")
                        .value("test-correlation-123"));
    }
}
