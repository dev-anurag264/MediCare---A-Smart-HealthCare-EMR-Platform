package com.medicare_health_systems.dto.request;

import jakarta.validation.constraints.*;
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
public class UpdatedDoctorProfileRequest {

    @Size(max = 100)
    private String specialty;          // null = don't change it

    @Size(max = 200)
    private String qualification;      // null = don't change it

    @Min(0) @Max(60)
    private Integer experienceYears;

    @DecimalMin("0.0") @DecimalMax("99999.99")
    private BigDecimal consultationFee;

    private String about;
    private LocalTime availableFrom;
    private LocalTime availableTo;

    @Min(15) @Max(120)
    private Integer slotDurationMinutes;

    private String availableDays;
}
