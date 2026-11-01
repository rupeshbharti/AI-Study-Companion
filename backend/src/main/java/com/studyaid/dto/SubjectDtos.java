package com.studyaid.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public class SubjectDtos {

    public record SubjectRequest(
            @NotBlank(message = "Subject name is required")
            String name,
            String icon,
            String color
    ) {}

    public record SubjectResponse(
            UUID id,
            String name,
            String icon,
            String color,
            long materialsCount,
            Instant createdAt
    ) {}
}

