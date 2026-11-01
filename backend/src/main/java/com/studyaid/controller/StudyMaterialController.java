package com.studyaid.controller;

import com.studyaid.dto.MaterialDtos.MaterialResponse;
import com.studyaid.entity.User;
import com.studyaid.service.StudyMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/materials")
@Tag(name = "Study Materials", description = "Student document and material storage (PDFs, DOCX, images)")
public class StudyMaterialController {

    private final StudyMaterialService materialService;

    public StudyMaterialController(StudyMaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    @Operation(summary = "Get list of uploaded study materials (optionally filtered by subject)")
    public ResponseEntity<List<MaterialResponse>> getMaterials(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) UUID subjectId) {
        return ResponseEntity.ok(materialService.getMaterials(user.getId(), subjectId));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a new study material file (PDF, image, document)")
    public ResponseEntity<MaterialResponse> uploadMaterial(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "subjectId", required = false) UUID subjectId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(materialService.uploadMaterial(user, subjectId, file));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an uploaded study material")
    public ResponseEntity<Void> deleteMaterial(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        materialService.deleteMaterial(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}

