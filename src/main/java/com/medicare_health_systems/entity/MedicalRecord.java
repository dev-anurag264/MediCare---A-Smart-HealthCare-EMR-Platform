package com.medicare_health_systems.entity;

import jakarta.persistence.*;
import lombok.*;
import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medical_records",
        uniqueConstraints = {
        //one appointment - exactly one medical record
        @UniqueConstraint(columnNames = "appointment_id", name = "uk_medical_record_appointment")
        },
        indexes = {
        @Index(columnList = "patient_id", name = "idx_medical_record_patient"),
                @Index(columnList = "doctor_id", name = "idx_medical_record_doctor")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "complaint", columnDefinition = "TEXT")
    private String complaint;

    //mapping
    @OneToMany(
            mappedBy = "medicalRecord",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Diagnosis> diagnoses = new ArrayList<>();

    @OneToMany(
            mappedBy = "medicalRecord",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Prescription> prescriptions = new ArrayList<>();

    @OneToMany(
            mappedBy = "medicalRecord",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<MedicalDocument> documents = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }



    public void addDiagnosis(Diagnosis diagnosis) {
        diagnoses.add(diagnosis);
        diagnosis.setMedicalRecord(this);
    }

    public void addPrescription(Prescription prescription) {
        prescriptions.add(prescription);
        prescription.setMedicalRecord(this);
    }

}
