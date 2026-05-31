package com.medicare_health_systems.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "doctor_profiles")
public class DoctorProfile {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "qualification",length = 100)
    private String qualification;
    @Column(name = "specialty", nullable = false, length = 100)
    private String speciality;
    @Column(name = "experience_years")
    private Integer experienceInYears;
    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFees;

    @Column(name = "about", columnDefinition = "TEXT")
    private String about;
    @Column(name = "available_from")
    @Builder.Default
    private LocalTime availableFrom = LocalTime.of(9,0);
    @Column(name = "available_to")
    @Builder.Default
    private LocalTime availableTo = LocalTime.of(17,0);

    @Column(name = "slot_duration_minutes")
    @Builder.Default
    private Integer slotDuration = 30;

    @Column(name = "available_days", length = 100)
    @Builder.Default
    private String availableDays = "MON,TUE,WED,THU,FRI";

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

}
