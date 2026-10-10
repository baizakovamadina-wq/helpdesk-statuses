
package com.example.helpdesk;

import com.example.helpdesk.domain.TicketNotifier;
import com.example.helpdesk.handler.TicketNotificationService;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class TicketNotificationServiceTest {

    @Test
    void shouldNotifyWhenTicketIsCreated() {


        TicketNotifier notifier = mock(TicketNotifier.class);


        TicketNotificationService service =
                new TicketNotificationService(notifier);


        service.notifyTicketCreated("HD-1042");

        verify(notifier, times(1))
                .notifyCreated("HD-1042");
    }
}
