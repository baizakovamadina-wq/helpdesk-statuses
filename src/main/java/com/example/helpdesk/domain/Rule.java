package com.example.helpdesk.domain;

public interface Rule {

    void check(TicketStatus from, TicketStatus to);
}