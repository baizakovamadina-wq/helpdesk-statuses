package com.example.helpdesk.handler;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @Operation(summary = "Create a new ticket")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Ticket created",
                    content = @Content(
                            schema = @Schema(
                                    implementation = TicketResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiError.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate business key",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiError.class
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<TicketResponse> create(
            @Valid @RequestBody CreateTicketRequest request
    ) {

        UUID id = UUID.randomUUID();

        service.register(
                id,
                request.businessKey(),
                request.title()
        );

        TicketResponse response = new TicketResponse(
                id,
                request.businessKey(),
                "OPEN",
                request.title()
        );

        return ResponseEntity
                .created(URI.create("/api/v1/tickets/" + id))
                .body(response);
    }

    @Operation(summary = "Get ticket by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ticket found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = TicketResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ticket not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiError.class
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> get(
            @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Start ticket - SENIOR only")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ticket moved to IN_PROGRESS",
                    content = @Content(
                            schema = @Schema(
                                    implementation = TicketResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiError.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "SENIOR role required",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiError.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ticket not found",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiError.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid status transition",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiError.class
                            )
                    )
            )
    })
    @PostMapping("/{id}/start")
    public ResponseEntity<TicketResponse> start(
            @PathVariable("id") UUID id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                service.start(
                        id,
                        authentication.getName()
                )
        );
    }
}