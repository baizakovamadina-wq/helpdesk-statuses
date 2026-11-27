package com.example.helpdesk.handler;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(

        @NotBlank
        String businessKey,

        @NotBlank
        @Size(max = 120)
        String title

) {
}
