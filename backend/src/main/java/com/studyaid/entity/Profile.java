package com.studyaid.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;

    @Column(name = "education_level", length = 50)
    private String educationLevel;

    @Lob
    @Column(name = "education_details")
    private String educationDetails; // JSON string

    @Column(name = "onboarding_completed")
    private boolean onboardingCompleted = false;

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    public Profile() {}

    public Profile(User user, String fullName) {
        this.user = user;
        this.fullName = fullName;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getEducationDetails() {
        return educationDetails;
    }

    public void setEducationDetails(String educationDetails) {
        this.educationDetails = educationDetails;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

