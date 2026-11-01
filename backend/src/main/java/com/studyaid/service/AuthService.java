package com.studyaid.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyaid.config.JwtTokenProvider;
import com.studyaid.dto.AuthDtos.*;
import com.studyaid.dto.ProfileDtos.ProfileResponse;
import com.studyaid.entity.Profile;
import com.studyaid.entity.User;
import com.studyaid.exception.BadRequestException;
import com.studyaid.repository.ProfileRepository;
import com.studyaid.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    public AuthService(UserRepository userRepository,
                       ProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = new User(request.email().toLowerCase().trim(), passwordEncoder.encode(request.password()));
        Profile profile = new Profile(user, request.fullName() != null ? request.fullName().trim() : "Student");
        user.setProfile(profile);

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail());
        UserSummary userSummary = new UserSummary(savedUser.getId(), savedUser.getEmail());
        ProfileResponse profileResponse = toProfileResponse(savedUser.getProfile());

        return new AuthResponse(token, userSummary, profileResponse);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail());
        UserSummary userSummary = new UserSummary(user.getId(), user.getEmail());
        ProfileResponse profileResponse = toProfileResponse(user.getProfile());

        return new AuthResponse(token, userSummary, profileResponse);
    }

    public ProfileResponse toProfileResponse(Profile profile) {
        if (profile == null) return null;
        Object detailsObj = null;
        if (profile.getEducationDetails() != null && !profile.getEducationDetails().isBlank()) {
            try {
                detailsObj = objectMapper.readValue(profile.getEducationDetails(), Object.class);
            } catch (JsonProcessingException e) {
                detailsObj = profile.getEducationDetails();
            }
        }
        return new ProfileResponse(
                profile.getId(),
                profile.getFullName(),
                profile.getAvatarUrl(),
                profile.getEducationLevel(),
                detailsObj,
                profile.isOnboardingCompleted(),
                profile.getUpdatedAt()
        );
    }
}

