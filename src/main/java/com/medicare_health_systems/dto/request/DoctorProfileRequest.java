package com.medicare_health_systems.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileRequest {
    @NotBlank(message = "Specialty is required")
    @Size(max = 100, message = "Specialty cannot exceed 100 characters")
    private String specialty;

    @Size(max = 200, message = "Qualification cannot exceed 200 characters")
    private String qualification;

    @Min(value = 0, message = "Experience years cannot be negative")
    @Max(value = 60, message = "Experience years seems too high")
    private Integer experienceYears;

    @DecimalMin(value = "0.0", message = "Consultation fee cannot be negative")
    @DecimalMax(value = "99999.99", message = "Consultation fee seems too high")
    private BigDecimal consultationFee;

    private String about;

    private LocalTime availableFrom;

    private LocalTime availableTo;

    @Min(value = 15, message = "Slot duration must be at least 15 minutes")
    @Max(value = 120, message = "Slot duration cannot exceed 120 minutes")
    private Integer slotDurationMinutes;

    // "MON,TUE,WED,THU,FRI"
    private String availableDays;
}
