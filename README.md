## Lab 4 — REST API and Configuration

In this lab, I added a REST API to the existing Helpdesk Ticket System using Spring Boot.

### Technologies

- Java 21
- Spring Boot 3.4.5
- PostgreSQL
- Flyway
- Maven
- Swagger / OpenAPI
- Spring Boot Actuator
- JUnit and Mockito

### REST API endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/v1/tickets | Create a new ticket |
| GET | /api/v1/tickets/{id} | Find a ticket by UUID |

### HTTP response codes

- 201 — Ticket created
- 200 — Ticket found
- 400 — Invalid request
- 404 — Ticket not found
- 409 — Duplicate business key
- 415 — Unsupported content type
- 500 — Internal server error

All API errors contain a code, message and correlation ID.

### Configuration

The application has two profiles: dev and prod. The dev profile is used for local development. The prod profile uses environment variables for database credentials.

Downstream configuration is managed using ConfigurationProperties. The development URL is http://localhost:9090 with a timeout of 2 seconds.

### Swagger and Actuator

Swagger UI: http://localhost:8080/swagger-ui/index.html

Available monitoring endpoints:

- /actuator/health
- /actuator/info

Sensitive endpoints such as /actuator/env and /actuator/heapdump are not exposed.

### Testing

The project includes tests for ticket status transitions, database transactions, duplicate protection, REST API validation, error responses, correlation IDs and configuration properties.

To verify the project, run:

`mvn -q verify`