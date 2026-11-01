package com.studyaid.repository;

import com.studyaid.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    List<Subject> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Subject> findByIdAndUserId(UUID id, UUID userId);
    long countByUserId(UUID userId);
}

