# Helpdesk

## Product

We build a Helpdesk product.



People track support tickets.

## Core item

The core item is a Ticket.
Each ticket has an ID and a status.

## Status table

| From | To | Result |
|---|---|---|
| OPEN | IN_PROGRESS | Allowed |
| IN_PROGRESS | RESOLVED | Allowed |
| OPEN | RESOLVED | Forbidden |
| RESOLVED | OPEN | Forbidden |

## Forbidden — why

OPEN → RESOLVED is forbidden because a ticket should be worked on before it is resolved.

RESOLVED → OPEN is forbidden because a resolved ticket cannot be reopened in this simplified workflow.
## Package Diagram

```text
com.example.helpdesk
│
├── config
│   ├── Application
│   └── RulesConfig
│
├── handler
│   └── TicketService
│
├── client
│   └── (empty)
│
├── dto
│   └── (empty)
│
└── domain
    ├── TicketId
    ├── TicketStatus
    ├── TicketPolicy
    ├── Rule
    ├── TransitionRule
    └── HoldRule
