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

`mvn -q verify`---

## Lab 5 — Authentication and Authorization

### Project Description

In this lab, I added authentication and authorization
to the Helpdesk Ticket System using Spring Security
and JWT tokens.

I also created a web interface inspired by a real
service desk portal.

### Technologies

- Java 21
- Spring Boot 3.4.5
- Spring Security
- OAuth2 Resource Server
- JWT (HS256)
- PostgreSQL
- Flyway
- Maven
- HTML, CSS and JavaScript
- JUnit 5 and MockMvc

### Development Accounts

| Username | Password | Role |
|----------|----------|------|
| junior | junior123 | JUNIOR |
| senior | senior123 | SENIOR |

These accounts are available only in the dev profile.

Passwords are encoded using BCrypt through
DelegatingPasswordEncoder.

### JWT Authentication

The application generates a JWT after successful login.

- Algorithm: HS256
- Issuer: helpdesk-statuses
- Token lifetime: 15 minutes (900 seconds)
- Claims: subject, issuer, roles, issuedAt, expiresAt
- Authorization header: Bearer token

The production JWT secret is provided through
the CSS_JWT_SECRET environment variable.

### API Endpoints

| Method | Endpoint | Access |
|--------|----------|--------|
| POST | /api/v1/auth/login | Public (dev only) |
| GET | /actuator/health | Public |
| GET | /api/me | JUNIOR, SENIOR |
| POST | /api/v1/tickets | JUNIOR, SENIOR |
| GET | /api/v1/tickets/{id} | JUNIOR, SENIOR |
| POST | /api/v1/tickets/{id}/start | SENIOR only |

### Role Permissions

| Action | JUNIOR | SENIOR |
|--------|--------|--------|
| Create ticket | Allowed | Allowed |
| View ticket | Allowed | Allowed |
| Start ticket | Forbidden | Allowed |

Only SENIOR can change a ticket status
from OPEN to IN_PROGRESS.

Authorization is implemented in both the
SecurityFilterChain and the service layer
using @PreAuthorize.

### Error Handling

The application returns consistent JSON errors
with code, message and correlationId.

- 400 — Validation error
- 401 — Missing, invalid or expired token
- 403 — Insufficient permissions
- 404 — Ticket not found
- 409 — Duplicate ticket or invalid status transition

### Web Interface

The web interface is available at:

http://localhost:8080/

The portal includes:

- Login and logout
- Current username and role
- Token expiration countdown
- Ticket creation
- Ticket search by UUID
- Ticket status management
- Role permissions table
- API response panel

JWT tokens are stored only in JavaScript memory.
Tokens are not stored in localStorage or sessionStorage.
Logout removes the token from browser memory.

### Automated Tests

The following test classes were added:

- SecurityTest
- JwtServiceTest
- AuthenticationTest
- ServiceSecurityTest

The tests cover:

- Successful authentication
- Incorrect credentials
- Missing JWT
- Invalid JWT
- Expired JWT
- Invalid signature
- JWT claims and lifetime
- Role-based access control
- Service-level authorization
- Public health endpoint

### How to Run

1. Start PostgreSQL.
2. Configure CSS_DEV_DB_PASSWORD.
3. Run Application.java with the dev profile.
4. Open http://localhost:8080/

Swagger UI:

http://localhost:8080/swagger-ui/index.html

### Build and Verification

Run:

    mvn verify

Or use IntelliJ IDEA:

Maven > Lifecycle > verify

### Conclusion

In this lab, I learned how to protect REST API
endpoints using Spring Security and JWT.

I implemented authentication, role-based access
control and service-level authorization.

I also created a web interface to demonstrate
how different users have different permissions
inside the application.

