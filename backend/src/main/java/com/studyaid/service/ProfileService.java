package com.studyaid.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyaid.dto.ProfileDtos.ProfileResponse;
import com.studyaid.dto.ProfileDtos.ProfileUpdateRequest;
import com.studyaid.entity.Profile;
import com.studyaid.entity.User;
import com.studyaid.exception.ResourceNotFoundException;
import com.studyaid.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final AuthService authService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    public ProfileService(ProfileRepository profileRepository,
                          AuthService authService,
                          FileStorageService fileStorageService,
                          ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.authService = authService;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    public ProfileResponse getProfile(UUID userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return authService.toProfileResponse(profile);
    }

    @Transactional
    public ProfileResponse updateProfile(User user, ProfileUpdateRequest request) {
        Profile profile = profileRepository.findById(user.getId())
                .orElseGet(() -> new Profile(user, user.getEmail()));

        if (request.fullName() != null) {
            profile.setFullName(request.fullName().trim());
        }
        if (request.educationLevel() != null) {
            profile.setEducationLevel(request.educationLevel());
        }
        if (request.educationDetails() != null) {
            try {
                profile.setEducationDetails(objectMapper.writeValueAsString(request.educationDetails()));
            } catch (JsonProcessingException e) {
                profile.setEducationDetails(request.educationDetails().toString());
            }
        }
        if (request.onboardingCompleted() != null) {
            profile.setOnboardingCompleted(request.onboardingCompleted());
        }
        profile.setUpdatedAt(Instant.now());

        Profile saved = profileRepository.save(profile);
        return authService.toProfileResponse(saved);
    }

    @Transactional
    public ProfileResponse uploadAvatar(User user, MultipartFile file) {
        Profile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        if (profile.getAvatarUrl() != null) {
            fileStorageService.deleteFile(profile.getAvatarUrl());
        }

        String fileUrl = fileStorageService.storeFile(file, "avatars", user.getId());
        profile.setAvatarUrl(fileUrl);
        profile.setUpdatedAt(Instant.now());

        Profile saved = profileRepository.save(profile);
        return authService.toProfileResponse(saved);
    }
}

