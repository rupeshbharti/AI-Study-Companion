package com.studyaid.service;

import com.studyaid.dto.AnalyticsDtos.DashboardStatsResponse;
import com.studyaid.dto.AnalyticsDtos.InsightsStatsResponse;
import com.studyaid.dto.ChatDtos.ChatSessionResponse;
import com.studyaid.entity.ChatSession;
import com.studyaid.entity.MessageRole;
import com.studyaid.repository.ChatMessageRepository;
import com.studyaid.repository.ChatSessionRepository;
import com.studyaid.repository.StudyMaterialRepository;
import com.studyaid.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final SubjectRepository subjectRepository;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final StudyMaterialRepository materialRepository;
    private final ChatService chatService;

    public AnalyticsService(SubjectRepository subjectRepository,
                            ChatSessionRepository sessionRepository,
                            ChatMessageRepository messageRepository,
                            StudyMaterialRepository materialRepository,
                            ChatService chatService) {
        this.subjectRepository = subjectRepository;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.materialRepository = materialRepository;
        this.chatService = chatService;
    }

    public DashboardStatsResponse getDashboardStats(UUID userId) {
        long subjects = subjectRepository.countByUserId(userId);
        long sessions = sessionRepository.countByUserId(userId);
        long messages = messageRepository.countByUserId(userId);
        long materials = materialRepository.countByUserId(userId);

        List<ChatSessionResponse> recent = sessionRepository.findTop4ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(chatService::toSessionResponse)
                .collect(Collectors.toList());

        return new DashboardStatsResponse(subjects, sessions, messages, materials, recent);
    }

    public InsightsStatsResponse getInsightsStats(UUID userId) {
        long subjects = subjectRepository.countByUserId(userId);
        long sessions = sessionRepository.countByUserId(userId);
        long userMsgs = messageRepository.countByUserIdAndRole(userId, MessageRole.USER);
        long aiMsgs = messageRepository.countByUserIdAndRole(userId, MessageRole.ASSISTANT);
        long materials = materialRepository.countByUserId(userId);

        Map<String, Long> breakdown = new HashMap<>();
        List<ChatSession> topSessions = sessionRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId);
        for (ChatSession s : topSessions) {
            String key = s.getSessionType().name().toLowerCase();
            breakdown.put(key, breakdown.getOrDefault(key, 0L) + 1);
        }

        return new InsightsStatsResponse(
                subjects,
                sessions,
                (userMsgs + aiMsgs),
                userMsgs,
                aiMsgs,
                materials,
                breakdown
        );
    }
}

