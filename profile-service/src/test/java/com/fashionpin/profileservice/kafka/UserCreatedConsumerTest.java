package com.fashionpin.profileservice.kafka;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fashionpin.common.event.UserCreatedEvent;
import com.fashionpin.profileservice.entity.Profile;
import com.fashionpin.profileservice.repository.ProcessedEventRepository;
import com.fashionpin.profileservice.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCreatedConsumerTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserCreatedConsumer userCreatedConsumer;

    private UserCreatedEvent event;

    @BeforeEach
    void setUp() {
        event = UserCreatedEvent.create(UUID.randomUUID().toString(), "createduser@fashionpin.com", "corr-1");
    }

    @Test
    void processEvent_NewEvent_CreatesProfile() {
        when(processedEventRepository.existsByEventId(event.getEventId())).thenReturn(false);
        when(profileRepository.existsByUserId(event.getUserId())).thenReturn(false);

        userCreatedConsumer.processEvent(event);

        verify(profileRepository, times(1)).save(any(Profile.class));
        verify(processedEventRepository, times(1)).save(any());
    }

    @Test
    void processEvent_DuplicateEvent_Skipped() {
        when(processedEventRepository.existsByEventId(event.getEventId())).thenReturn(true);

        userCreatedConsumer.processEvent(event);

        verify(profileRepository, never()).save(any());
    }
}
