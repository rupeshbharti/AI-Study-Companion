package com.studyaid.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ChatDtos {

    public record ChatSessionRequest(
            UUID subjectId,
            String sessionType, // general, tutorial, assignment, homework
            String title
    ) {}

    public record SubjectBrief(
            UUID id,
            String name,
            String icon,
            String color
    ) {}

    public record ChatSessionResponse(
            UUID id,
            UUID userId,
            UUID subjectId,
            SubjectBrief subject,
            String sessionType,
            String title,
            Instant createdAt
    ) {}

    public record AttachmentDto(
            String type, // image, document
            String mimeType,
            String base64,
            String extractedText,
            String name
    ) {}

    public record MessageRequest(
            String content,
            List<AttachmentDto> attachments
    ) {}

    public record MessageResponse(
            UUID id,
            UUID sessionId,
            UUID userId,
            String role,
            String content,
            List<AttachmentDto> attachments,
            Instant createdAt
    ) {}

    public record GeminiStreamRequest(
            String message,
            UUID sessionId,
            String subjectName,
            String sectionType,
            List<AttachmentDto> attachments,
            String model
    ) {}
}

