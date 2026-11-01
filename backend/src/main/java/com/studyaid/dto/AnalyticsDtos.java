package com.studyaid.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsDtos {

    public record DashboardStatsResponse(
            long subjects,
            long sessions,
            long messages,
            long materials,
            List<ChatDtos.ChatSessionResponse> recentSessions
    ) {}

    public record InsightsStatsResponse(
            long subjects,
            long sessions,
            long messages,
            long userMessages,
            long aiMessages,
            long materials,
            Map<String, Long> sessionTypeBreakdown
    ) {}
}

