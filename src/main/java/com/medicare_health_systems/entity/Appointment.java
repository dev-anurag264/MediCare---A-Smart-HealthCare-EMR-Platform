package com.medicare_health_systems.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments",
        indexes = {
        @Index(columnList = "patient_id",name= "idx_appointments_patient"),
                @Index(columnList = "doctor_id",name= "idx_appointments_doctor"),
                @Index(columnList = "appointment_date", name = "idx_appointment_date"),
                @Index(columnList = "status", name = "idx_appointment_status")
        }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; //startTime

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Version
    @Column(name = "version")
    private Long version;

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
    public void transitionTo(AppointmentStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot transition appointment from %s to %s", this.status, newStatus)
            );
        }
        this.status = newStatus;
    }

}
