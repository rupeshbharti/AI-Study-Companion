package com.studyaid.repository;

import com.studyaid.entity.StudyMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, UUID> {
    List<StudyMaterial> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<StudyMaterial> findByUserIdAndSubjectIdOrderByCreatedAtDesc(UUID userId, UUID subjectId);
    Optional<StudyMaterial> findByIdAndUserId(UUID id, UUID userId);
    long countByUserId(UUID userId);
    long countBySubjectId(UUID subjectId);
}

