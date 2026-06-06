package com.medicare_health_systems.dto.request;

import com.medicare_health_systems.entity.DiagnosisSeverity;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class CreateMedicalRequest {
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @Size(max = 1000, message = "Chief complaint cannot exceed 1000 characters")
    private String complaint;

    /** At least one diagnosis is required to create a meaningful record */
    @NotEmpty(message = "At least one diagnosis is required")
    private List<DiagnosisRequest> diagnoses;

    /** Prescriptions are optional — not every visit needs medicine */
    private List<PrescriptionRequest> prescriptions;



    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosisRequest {


        @Size(max = 20, message = "ICD code cannot exceed 20 characters")
        private String icdCode;

        @NotBlank(message = "Diagnosis description is required")
        private String description;

        private DiagnosisSeverity severity;

        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionRequest {

        @NotBlank(message = "Medicine name is required")
        @Size(max = 200, message = "Medicine name too long")
        private String medicineName;

        private String dosage;

        private String frequency;

        @Min(value = 1, message = "Duration must be at least 1 day")
        @Max(value = 365, message = "Duration cannot exceed 365 days")
        private Integer durationDays;

        private String instructions;
    }
}
