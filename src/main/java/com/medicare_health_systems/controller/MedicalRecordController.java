package com.medicare_health_systems.controller;

import com.medicare_health_systems.dto.request.CreateMedicalRequest;
import com.medicare_health_systems.dto.request.MedicalRecordResponse;
import com.medicare_health_systems.service.MedicalRecordService;
import com.medicare_health_systems.utils.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH + "/medicalrecord")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordResponse> createMedicalRecord(
            @Valid @RequestBody CreateMedicalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicalRecordService.createMedicalRecord(request));
    }


    //get medical record by id
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MedicalRecordResponse> getRecordById(@PathVariable Long id){
        return ResponseEntity.ok(medicalRecordService.getMedicalRecordById(id));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<MedicalRecordResponse> getByAppointment(@PathVariable Long id){
        return  ResponseEntity.ok(medicalRecordService.getRecordByAppointmentId(id));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Page<MedicalRecordResponse>> getPatientRecords(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(medicalRecordService.getPatientRecords(patientId,page,size));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<MedicalRecordResponse>> getMyRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(medicalRecordService.getMyRecordsAsDoctor(page, size));
    }
}
