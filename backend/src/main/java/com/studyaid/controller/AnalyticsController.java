package com.studyaid.controller;

import com.studyaid.dto.AnalyticsDtos.DashboardStatsResponse;
import com.studyaid.dto.AnalyticsDtos.InsightsStatsResponse;
import com.studyaid.entity.User;
import com.studyaid.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Dashboard counts and student learning insights")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard aggregate counts and recent sessions")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getDashboardStats(user.getId()));
    }

    @GetMapping("/insights")
    @Operation(summary = "Get detailed learning insights, message counts and activity breakdown")
    public ResponseEntity<InsightsStatsResponse> getInsightsStats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getInsightsStats(user.getId()));
    }
}

