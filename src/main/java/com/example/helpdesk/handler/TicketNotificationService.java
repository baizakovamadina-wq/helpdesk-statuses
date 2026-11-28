
package com.example.helpdesk.handler;

import com.example.helpdesk.domain.TicketNotifier;

public class TicketNotificationService {

    private final TicketNotifier notifier;

    public TicketNotificationService(TicketNotifier notifier) {
        this.notifier = notifier;
    }

    public void notifyTicketCreated(String businessKey) {
        if (businessKey == null || businessKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Business key cannot be blank"
            );
        }

        notifier.notifyCreated(businessKey);
    }
}
