package com.studyaid.service;

import com.studyaid.dto.ChatDtos.SubjectBrief;
import com.studyaid.dto.MaterialDtos.MaterialResponse;
import com.studyaid.entity.StudyMaterial;
import com.studyaid.entity.Subject;
import com.studyaid.entity.User;
import com.studyaid.exception.ResourceNotFoundException;
import com.studyaid.repository.StudyMaterialRepository;
import com.studyaid.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudyMaterialService {

    private final StudyMaterialRepository materialRepository;
    private final SubjectRepository subjectRepository;
    private final FileStorageService fileStorageService;

    public StudyMaterialService(StudyMaterialRepository materialRepository,
                                SubjectRepository subjectRepository,
                                FileStorageService fileStorageService) {
        this.materialRepository = materialRepository;
        this.subjectRepository = subjectRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<MaterialResponse> getMaterials(UUID userId, UUID subjectId) {
        List<StudyMaterial> materials;
        if (subjectId != null) {
            materials = materialRepository.findByUserIdAndSubjectIdOrderByCreatedAtDesc(userId, subjectId);
        } else {
            materials = materialRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return materials.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public MaterialResponse uploadMaterial(User user, UUID subjectId, MultipartFile file) {
        Subject subject = null;
        if (subjectId != null) {
            subject = subjectRepository.findByIdAndUserId(subjectId, user.getId())
                    .orElse(null);
        }

        String fileUrl = fileStorageService.storeFile(file, "materials", user.getId());
        String fileType = determineFileType(file);

        StudyMaterial material = new StudyMaterial(
                user,
                subject,
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "study_material",
                fileUrl,
                fileType,
                file.getSize()
        );

        StudyMaterial saved = materialRepository.save(material);
        return toResponse(saved);
    }

    @Transactional
    public void deleteMaterial(UUID id, UUID userId) {
        StudyMaterial material = materialRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study material not found"));

        fileStorageService.deleteFile(material.getFileUrl());
        materialRepository.delete(material);
    }

    private String determineFileType(MultipartFile file) {
        String contentType = file.getContentType();
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        if (contentType != null && contentType.startsWith("image/")) return "image";
        if ((contentType != null && contentType.contains("pdf")) || name.endsWith(".pdf")) return "pdf";
        if ((contentType != null && (contentType.contains("word") || contentType.contains("document"))) || name.endsWith(".docx") || name.endsWith(".doc")) return "doc";
        return "other";
    }

    public MaterialResponse toResponse(StudyMaterial m) {
        SubjectBrief brief = null;
        if (m.getSubject() != null) {
            Subject s = m.getSubject();
            brief = new SubjectBrief(s.getId(), s.getName(), s.getIcon(), s.getColor());
        }

        return new MaterialResponse(
                m.getId(),
                m.getSubject() != null ? m.getSubject().getId() : null,
                brief,
                m.getName(),
                m.getFileUrl(),
                m.getFileType(),
                m.getFileSize(),
                m.getCreatedAt()
        );
    }
}

