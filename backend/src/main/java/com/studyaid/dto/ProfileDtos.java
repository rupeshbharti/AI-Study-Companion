package com.studyaid.dto;

import java.time.Instant;
import java.util.UUID;

public class ProfileDtos {

    public record ProfileUpdateRequest(
            String fullName,
            String educationLevel,
            Object educationDetails,
            Boolean onboardingCompleted
    ) {}

    public record ProfileResponse(
            UUID id,
            String fullName,
            String avatarUrl,
            String educationLevel,
            Object educationDetails,
            boolean onboardingCompleted,
            Instant updatedAt
    ) {}
}

