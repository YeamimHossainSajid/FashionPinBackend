package com.fashionpin.userservice.kafka;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fashionpin.common.event.UserRegisteredEvent;
import com.fashionpin.userservice.entity.User;
import com.fashionpin.userservice.repository.ProcessedEventRepository;
import com.fashionpin.userservice.repository.UserRepository;
import com.fashionpin.userservice.service.OutboxService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRegisteredConsumerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private OutboxService outboxService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserRegisteredConsumer userRegisteredConsumer;

    private UserRegisteredEvent event;

    @BeforeEach
    void setUp() {
        event = UserRegisteredEvent.create(UUID.randomUUID().toString(), "newuser@fashionpin.com", "corr-1");
    }

    @Test
    void processEvent_NewEvent_CreatesUserAndOutboxEvent() {
        when(processedEventRepository.existsByEventId(event.getEventId())).thenReturn(false);
        when(userRepository.existsById(event.getUserId())).thenReturn(false);

        userRegisteredConsumer.processEvent(event);

        verify(userRepository, times(1)).save(any(User.class));
        verify(outboxService, times(1)).saveEvent(eq("User"), eq(event.getUserId()), eq("UserCreated"), any());
        verify(processedEventRepository, times(1)).save(any());
    }

    @Test
    void processEvent_DuplicateEvent_Skipped() {
        when(processedEventRepository.existsByEventId(event.getEventId())).thenReturn(true);

        userRegisteredConsumer.processEvent(event);

        verify(userRepository, never()).save(any());
        verify(outboxService, never()).saveEvent(any(), any(), any(), any());
    }
}
