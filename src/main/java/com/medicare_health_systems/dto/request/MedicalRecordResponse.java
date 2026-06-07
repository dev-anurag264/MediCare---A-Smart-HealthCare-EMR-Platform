package com.medicare_health_systems.dto.request;
import com.medicare_health_systems.entity.DiagnosisSeverity;
import com.medicare_health_systems.entity.DocumentType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponse {
    private Long id;
    private Long appointmentId;

    private Long patientId;
    private String patientName;

    private Long doctorId;
    private String doctorName;

    private LocalDate visitDate;
    private String chiefComplaint;

    private List<DiagnosisResponse> diagnoses;
    private List<PrescriptionResponse> prescriptions;
    private List<MedicalDocumentResponse> documents;

    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosisResponse {
        private Long id;
        private String icdCode;
        private String description;
        private DiagnosisSeverity severity;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionResponse {
        private Long id;
        private String medicineName;
        private String dosage;
        private String frequency;
        private Integer durationDays;
        private String instructions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicalDocumentResponse {
        private Long id;
        private String originalName;
        private String fileType;
        private Long fileSize;
        private DocumentType documentType;
        private String downloadUrl;
        private LocalDateTime createdAt;
    }
}
