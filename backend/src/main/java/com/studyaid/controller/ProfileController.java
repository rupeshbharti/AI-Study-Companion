package com.studyaid.controller;

import com.studyaid.dto.ProfileDtos.ProfileResponse;
import com.studyaid.dto.ProfileDtos.ProfileUpdateRequest;
import com.studyaid.entity.User;
import com.studyaid.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "Student profile, education tier details, and avatar management")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    @Operation(summary = "Get the student profile of the authenticated user")
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.getProfile(user.getId()));
    }

    @PutMapping
    @Operation(summary = "Update student profile (full name, education level, education details, onboarding)")
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(user, request));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload student profile picture / avatar")
    public ResponseEntity<ProfileResponse> uploadAvatar(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadAvatar(user, file));
    }
}

