package com.studyaid.repository;

import com.studyaid.entity.ChatSession;
import com.studyaid.entity.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    Optional<ChatSession> findFirstByUserIdAndSessionTypeAndSubjectIsNullOrderByCreatedAtDesc(
            UUID userId, SessionType sessionType);

    Optional<ChatSession> findFirstByUserIdAndSubjectIdAndSessionTypeOrderByCreatedAtDesc(
            UUID userId, UUID subjectId, SessionType sessionType);

    long countByUserId(UUID userId);
    
    List<ChatSession> findTop4ByUserIdOrderByCreatedAtDesc(UUID userId);
    List<ChatSession> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);
}

