package com.fashionpin.userservice.service;

import com.fashionpin.common.exception.BusinessException;
import com.fashionpin.common.exception.ResourceNotFoundException;
import com.fashionpin.userservice.dto.UpdateUserRequest;
import com.fashionpin.userservice.dto.UserResponse;
import com.fashionpin.userservice.entity.User;
import com.fashionpin.userservice.repository.UserRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String currentUserId) {
        return getUserById(currentUserId);
    }

    @Transactional
    public UserResponse updateCurrentUser(String currentUserId, UpdateUserRequest request) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.findByEmail(newEmail).isPresent()) {
                throw new BusinessException("EMAIL_ALREADY_EXISTS", "Email is already taken");
            }
            user.setEmail(newEmail);
        }

        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteCurrentUser(String currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));
        user.setStatus("DELETED");
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
