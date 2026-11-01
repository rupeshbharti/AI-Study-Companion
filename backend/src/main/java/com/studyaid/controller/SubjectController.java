package com.studyaid.controller;

import com.studyaid.dto.SubjectDtos.SubjectRequest;
import com.studyaid.dto.SubjectDtos.SubjectResponse;
import com.studyaid.entity.User;
import com.studyaid.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subjects")
@Tag(name = "Subjects", description = "Subject management for students")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    @Operation(summary = "List all subjects belonging to the authenticated student")
    public ResponseEntity<List<SubjectResponse>> getSubjects(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(subjectService.getSubjects(user.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specific subject by ID")
    public ResponseEntity<SubjectResponse> getSubject(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(subjectService.getSubject(id, user.getId()));
    }

    @PostMapping
    @Operation(summary = "Create a new subject")
    public ResponseEntity<SubjectResponse> createSubject(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(subjectService.createSubject(user, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing subject")
    public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(subjectService.updateSubject(id, user, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a subject")
    public ResponseEntity<Void> deleteSubject(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        subjectService.deleteSubject(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}

