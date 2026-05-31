package com.medicare_health_systems.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String specialty;
    private String qualification;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private String about;
    private LocalTime availableFrom;
    private LocalTime availableTo;
    private Integer slotDurationMinutes;
    private String availableDays;
}
