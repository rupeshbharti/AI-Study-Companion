package com.studyaid.controller;

import com.studyaid.dto.AuthDtos.*;
import com.studyaid.dto.ProfileDtos.ProfileResponse;
import com.studyaid.entity.User;
import com.studyaid.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, authentication and session check")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new student user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user and profile")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        UserSummary userSummary = new UserSummary(user.getId(), user.getEmail());
        ProfileResponse profileResponse = authService.toProfileResponse(user.getProfile());
        return ResponseEntity.ok(new AuthResponse(null, userSummary, profileResponse));
    }
}

