package com.fashionpin.userservice.controller;

import com.fashionpin.common.dto.ApiResponse;
import com.fashionpin.userservice.dto.UpdateUserRequest;
import com.fashionpin.userservice.dto.UserResponse;
import com.fashionpin.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/api/users/{id}", "/api/v1/user/{id}"})
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable("id") String id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping({"/api/users/me", "/api/v1/user/me"})
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Principal principal) {
        String currentUserId = principal.getName();
        UserResponse response = userService.getCurrentUser(currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping({"/api/users/me", "/api/v1/user/me"})
    @Operation(summary = "Update current authenticated user details")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            Principal principal,
            @Valid @RequestBody UpdateUserRequest request) {
        String currentUserId = principal.getName();
        UserResponse response = userService.updateCurrentUser(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.ok("User updated successfully", response));
    }

    @DeleteMapping({"/api/users/me", "/api/v1/user/me"})
    @Operation(summary = "Delete / deactivate current authenticated user account")
    public ResponseEntity<ApiResponse<Void>> deleteCurrentUser(Principal principal) {
        String currentUserId = principal.getName();
        userService.deleteCurrentUser(currentUserId);
        return ResponseEntity.ok(ApiResponse.ok("User account deleted successfully", null));
    }
}
