package com.studyaid.controller;

import com.studyaid.dto.ChatDtos.*;
import com.studyaid.entity.MessageRole;
import com.studyaid.entity.User;
import com.studyaid.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat & Gemini AI", description = "Chat sessions, message history, and real-time Gemini streaming")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/sessions")
    @Operation(summary = "List all chat sessions for the student")
    public ResponseEntity<List<ChatSessionResponse>> getSessions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatService.getSessions(user.getId()));
    }

    @PostMapping("/sessions")
    @Operation(summary = "Get or create a chat session (general or subject tutorial/assignment/homework)")
    public ResponseEntity<ChatSessionResponse> getOrCreateSession(
            @AuthenticationPrincipal User user,
            @RequestBody ChatSessionRequest request) {
        return ResponseEntity.ok(chatService.getOrCreateSession(user, request));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "Get message history for a specific session")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatService.getMessages(sessionId, user.getId()));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "Save a message directly to a chat session")
    public ResponseEntity<MessageResponse> saveMessage(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal User user,
            @RequestBody MessageRequest request,
            @RequestParam(defaultValue = "user") String role) {
        return ResponseEntity.ok(chatService.saveMessage(sessionId, user, request, MessageRole.fromString(role)));
    }

    @PostMapping(value = "/stream")
    @Operation(summary = "Stream adaptive AI response from Google Gemini via chunked streaming")
    public ResponseBodyEmitter streamChat(
            @AuthenticationPrincipal User user,
            @RequestBody GeminiStreamRequest request) {
        return chatService.streamChat(user, request);
    }
}

