package com.medicare_health_systems.controller;

import com.medicare_health_systems.dto.request.MedicalRecordResponse;
import com.medicare_health_systems.entity.DocumentType;
import com.medicare_health_systems.repository.MedicalDocumentRepository;
import com.medicare_health_systems.service.FileStorageService;
import com.medicare_health_systems.service.MedicalRecordService;
import com.medicare_health_systems.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/documents")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {
    private final MedicalRecordService medicalRecordService;
    private final FileStorageService fileStorageService;
    private final MedicalDocumentRepository medicalDocumentRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MedicalRecordResponse.MedicalDocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long medicalRecordId,
            @RequestParam(required = false, defaultValue = "OTHER") DocumentType documentType) {

        return ResponseEntity.ok(
                medicalRecordService.uploadDocument(file, patientId, medicalRecordId, documentType)
        );
    }
}
