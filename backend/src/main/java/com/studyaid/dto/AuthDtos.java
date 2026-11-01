package com.studyaid.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Must be a valid email format")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 6, message = "Password must be at least 6 characters")
            String password,

            String fullName
    ) {}

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Must be a valid email format")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    public record AuthResponse(
            String token,
            String tokenType,
            UserSummary user,
            ProfileDtos.ProfileResponse profile
    ) {
        public AuthResponse(String token, UserSummary user, ProfileDtos.ProfileResponse profile) {
            this(token, "Bearer", user, profile);
        }
    }

    public record UserSummary(
            UUID id,
            String email
    ) {}
}

