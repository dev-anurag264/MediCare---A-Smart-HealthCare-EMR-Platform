package com.medicare_health_systems.controller;

import com.medicare_health_systems.dto.request.CreateMedicalRequest;
import com.medicare_health_systems.dto.request.MedicalRecordResponse;
import com.medicare_health_systems.service.MedicalRecordService;
import com.medicare_health_systems.utils.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private MedicalRecordService medicalRecordService;
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordResponse> createMedicalRecord(
            @Valid @RequestBody CreateMedicalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicalRecordService.createMedicalRecord(request));
    }
}
