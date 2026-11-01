package com.studyaid.repository;

import com.studyaid.entity.ChatMessage;
import com.studyaid.entity.MessageRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
    List<ChatMessage> findTop20BySessionIdOrderByCreatedAtDesc(UUID sessionId);
    long countByUserId(UUID userId);
    long countByUserIdAndRole(UUID userId, MessageRole role);
}

