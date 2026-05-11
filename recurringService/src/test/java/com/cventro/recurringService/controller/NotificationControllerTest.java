package com.cventro.recurringService.controller;

import com.cventro.recurringService.entity.NotificationEvent;
import com.cventro.recurringService.enums.Status;
import com.cventro.recurringService.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void createEventRejectsInvalidSmsPhoneNumber() throws Exception {
        String request = """
                {
                  "userId": "user-1",
                  "type": "SMS",
                  "scheduleType": "FIXED",
                  "payload": {
                    "message": "hello",
                    "phoneNumber": "1234567890"
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/events/create-event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEventAcceptsValidSmsPhoneNumber() throws Exception {
        when(notificationService.createNotificationEvent(ArgumentMatchers.any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String request = """
                {
                  "userId": "user-1",
                  "type": "SMS",
                  "scheduleType": "FIXED",
                  "payload": {
                    "message": "hello",
                    "phoneNumber": "+919876543210"
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/events/create-event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());
    }

    @Test
    void stopEventCancelsEventById() throws Exception {
        NotificationEvent cancelledEvent = NotificationEvent.builder()
                .eventId("event-1")
                .status(Status.CANCELLED)
                .build();

        when(notificationService.stopNotificationEvent("event-1")).thenReturn(cancelledEvent);

        mockMvc.perform(post("/api/v1/events/stop-event/event-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("event-1"))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
