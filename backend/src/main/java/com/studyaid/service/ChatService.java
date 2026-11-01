package com.studyaid.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyaid.dto.ChatDtos.*;
import com.studyaid.entity.*;
import com.studyaid.exception.ResourceNotFoundException;
import com.studyaid.repository.ChatMessageRepository;
import com.studyaid.repository.ChatSessionRepository;
import com.studyaid.repository.SubjectRepository;
import com.studyaid.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public ChatService(ChatSessionRepository sessionRepository,
                       ChatMessageRepository messageRepository,
                       SubjectRepository subjectRepository,
                       UserRepository userRepository,
                       GeminiService geminiService,
                       ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
    }

    public List<ChatSessionResponse> getSessions(UUID userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toSessionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatSessionResponse getOrCreateSession(User user, ChatSessionRequest request) {
        SessionType sessionType = SessionType.fromString(request.sessionType());

        if (request.subjectId() == null) {
            // General chat session
            Optional<ChatSession> existing = sessionRepository
                    .findFirstByUserIdAndSessionTypeAndSubjectIsNullOrderByCreatedAtDesc(user.getId(), sessionType);

            if (existing.isPresent()) {
                return toSessionResponse(existing.get());
            }

            ChatSession session = new ChatSession(user, null, sessionType,
                    request.title() != null ? request.title() : "General Chat");
            ChatSession saved = sessionRepository.save(session);
            return toSessionResponse(saved);
        } else {
            // Subject-specific session
            Subject subject = subjectRepository.findByIdAndUserId(request.subjectId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

            Optional<ChatSession> existing = sessionRepository
                    .findFirstByUserIdAndSubjectIdAndSessionTypeOrderByCreatedAtDesc(user.getId(), subject.getId(), sessionType);

            if (existing.isPresent()) {
                return toSessionResponse(existing.get());
            }

            String title = (request.title() != null && !request.title().isBlank())
                    ? request.title() : subject.getName() + " - " + sessionType.name().toLowerCase();
            ChatSession session = new ChatSession(user, subject, sessionType, title);
            ChatSession saved = sessionRepository.save(session);
            return toSessionResponse(saved);
        }
    }

    public List<MessageResponse> getMessages(UUID sessionId, UUID userId) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Chat session not found");
        }

        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse saveMessage(UUID sessionId, User user, MessageRequest request, MessageRole role) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        String attachmentsJson = null;
        if (request.attachments() != null && !request.attachments().isEmpty()) {
            try {
                attachmentsJson = objectMapper.writeValueAsString(request.attachments());
            } catch (Exception ignored) {}
        }

        ChatMessage message = new ChatMessage(session, user, role, request.content(), attachmentsJson);
        ChatMessage saved = messageRepository.save(message);
        return toMessageResponse(saved);
    }

    @Transactional
    public ResponseBodyEmitter streamChat(User user, GeminiStreamRequest request) {
        ChatSession session = sessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

        // Save user message to database
        saveMessage(session.getId(), user, new MessageRequest(request.message(), request.attachments()), MessageRole.USER);

        // Fetch recent messages for history
        List<ChatMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());

        ResponseBodyEmitter emitter = new ResponseBodyEmitter(Duration.ofMinutes(3).toMillis());

        UUID sessionId = session.getId();
        UUID userId = user.getId();

        // Asynchronously call Gemini and save assistant response when complete
        geminiService.streamGeminiResponse(
                user.getProfile(),
                request,
                history,
                emitter,
                fullResponseText -> {
                    if (fullResponseText != null && !fullResponseText.isBlank()) {
                        saveAssistantMessage(sessionId, userId, fullResponseText.trim());
                    }
                }
        );

        return emitter;
    }

    @Transactional
    public void saveAssistantMessage(UUID sessionId, UUID userId, String content) {
        try {
            Optional<ChatSession> sessionOpt = sessionRepository.findById(sessionId);
            Optional<User> userOpt = userRepository.findById(userId);
            if (sessionOpt.isPresent() && userOpt.isPresent()) {
                ChatMessage assistantMsg = new ChatMessage(sessionOpt.get(), userOpt.get(), MessageRole.ASSISTANT, content, null);
                messageRepository.save(assistantMsg);
            }
        } catch (Exception ex) {
            // Log but don't disrupt streaming
        }
    }

    public ChatSessionResponse toSessionResponse(ChatSession session) {
        SubjectBrief brief = null;
        if (session.getSubject() != null) {
            Subject s = session.getSubject();
            brief = new SubjectBrief(s.getId(), s.getName(), s.getIcon(), s.getColor());
        }

        return new ChatSessionResponse(
                session.getId(),
                session.getUser().getId(),
                session.getSubject() != null ? session.getSubject().getId() : null,
                brief,
                session.getSessionType().name().toLowerCase(),
                session.getTitle(),
                session.getCreatedAt()
        );
    }

    public MessageResponse toMessageResponse(ChatMessage message) {
        List<AttachmentDto> atts = Collections.emptyList();
        if (message.getAttachments() != null && !message.getAttachments().isBlank()) {
            try {
                atts = objectMapper.readValue(message.getAttachments(), new TypeReference<List<AttachmentDto>>() {});
            } catch (Exception ignored) {}
        }

        return new MessageResponse(
                message.getId(),
                message.getSession().getId(),
                message.getUser().getId(),
                message.getRole().name().toLowerCase(),
                message.getContent(),
                atts,
                message.getCreatedAt()
        );
    }
}
