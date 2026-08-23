package com.fashionpin.userservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fashionpin.common.exception.BusinessException;
import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.userservice.dto.UpdateUserRequest;
import com.fashionpin.userservice.dto.UserResponse;
import com.fashionpin.userservice.entity.User;
import com.fashionpin.userservice.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(UUID.randomUUID().toString())
                .email("user@fashionpin.com")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));

        UserResponse response = userService.getUserById(sampleUser.getId());

        assertNotNull(response);
        assertEquals(sampleUser.getId(), response.getId());
        assertEquals("user@fashionpin.com", response.getEmail());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById("nonexistent"));
    }

    @Test
    void updateCurrentUser_Success() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("newemail@fashionpin.com")
                .build();

        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));
        when(userRepository.findByEmail("newemail@fashionpin.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = userService.updateCurrentUser(sampleUser.getId(), request);

        assertNotNull(response);
        assertEquals("newemail@fashionpin.com", response.getEmail());
    }

    @Test
    void updateCurrentUser_DuplicateEmail_ThrowsException() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email("existing@fashionpin.com")
                .build();

        User existingUser = User.builder().id("other-id").email("existing@fashionpin.com").build();

        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));
        when(userRepository.findByEmail("existing@fashionpin.com")).thenReturn(Optional.of(existingUser));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.updateCurrentUser(sampleUser.getId(), request));
        assertEquals("EMAIL_ALREADY_EXISTS", ex.getCode());
    }

    @Test
    void deleteCurrentUser_Success() {
        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.deleteCurrentUser(sampleUser.getId());

        assertEquals("DELETED", sampleUser.getStatus());
        verify(userRepository, times(1)).save(sampleUser);
    }
}
