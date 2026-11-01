package com.studyaid.dto;

import java.time.Instant;
import java.util.UUID;

public class MaterialDtos {

    public record MaterialResponse(
            UUID id,
            UUID subjectId,
            ChatDtos.SubjectBrief subject,
            String name,
            String fileUrl,
            String fileType,
            Long fileSize,
            Instant createdAt
    ) {}
}

