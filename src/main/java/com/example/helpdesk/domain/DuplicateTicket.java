
package com.example.helpdesk.domain;

public class DuplicateTicket extends RuntimeException {

    public DuplicateTicket(String businessKey) {
        super("Duplicate ticket: " + businessKey);
    }
}
