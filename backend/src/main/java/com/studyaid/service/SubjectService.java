package com.studyaid.service;

import com.studyaid.dto.SubjectDtos.SubjectRequest;
import com.studyaid.dto.SubjectDtos.SubjectResponse;
import com.studyaid.entity.Subject;
import com.studyaid.entity.User;
import com.studyaid.exception.ResourceNotFoundException;
import com.studyaid.repository.StudyMaterialRepository;
import com.studyaid.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final StudyMaterialRepository studyMaterialRepository;

    public SubjectService(SubjectRepository subjectRepository, StudyMaterialRepository studyMaterialRepository) {
        this.subjectRepository = subjectRepository;
        this.studyMaterialRepository = studyMaterialRepository;
    }

    public List<SubjectResponse> getSubjects(UUID userId) {
        return subjectRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toSubjectResponse)
                .collect(Collectors.toList());
    }

    public SubjectResponse getSubject(UUID id, UUID userId) {
        Subject subject = subjectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        return toSubjectResponse(subject);
    }

    @Transactional
    public SubjectResponse createSubject(User user, SubjectRequest request) {
        Subject subject = new Subject(
                user,
                request.name().trim(),
                request.icon() != null ? request.icon() : "📚",
                request.color() != null ? request.color() : "#6366f1"
        );
        Subject saved = subjectRepository.save(subject);
        return toSubjectResponse(saved);
    }

    @Transactional
    public SubjectResponse updateSubject(UUID id, User user, SubjectRequest request) {
        Subject subject = subjectRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        if (request.name() != null && !request.name().isBlank()) {
            subject.setName(request.name().trim());
        }
        if (request.icon() != null) {
            subject.setIcon(request.icon());
        }
        if (request.color() != null) {
            subject.setColor(request.color());
        }

        Subject saved = subjectRepository.save(subject);
        return toSubjectResponse(saved);
    }

    @Transactional
    public void deleteSubject(UUID id, UUID userId) {
        Subject subject = subjectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        subjectRepository.delete(subject);
    }

    public SubjectResponse toSubjectResponse(Subject subject) {
        long materialsCount = studyMaterialRepository.countBySubjectId(subject.getId());
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getIcon(),
                subject.getColor(),
                materialsCount,
                subject.getCreatedAt()
        );
    }
}

