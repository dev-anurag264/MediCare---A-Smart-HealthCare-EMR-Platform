package com.medicare_health_systems.controller;

import com.medicare_health_systems.dto.request.DoctorProfileRequest;
import com.medicare_health_systems.dto.request.UpdatedDoctorProfileRequest;
import com.medicare_health_systems.dto.response.DoctorProfileResponse;
import com.medicare_health_systems.service.DoctorService;
import com.medicare_health_systems.utils.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstants.DOCTORS_PATH)
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    @PostMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> createProfile(
            @Valid @RequestBody DoctorProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doctorService.createProfile(request));
    }

    /**
     * PUT — Update existing profile
     * All fields optional — only sends what needs changing
     * Returns 404 if no profile exists yet — use POST first
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> updateProfile(
            @Valid @RequestBody UpdatedDoctorProfileRequest request) {
        return ResponseEntity.ok(doctorService.updateProfile(request));
    }
}
