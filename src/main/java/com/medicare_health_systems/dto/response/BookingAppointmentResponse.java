package com.medicare_health_systems.dto.response;

import com.medicare_health_systems.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingAppointmentResponse {
    private Long id;

    // Patient info (flattened)
    private Long patientId;
    private String patientName;     // firstName + lastName combined

    // Doctor info (flattened)
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialty;

    // Appointment details
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentStatus status;
    private String reason;
    private String notes;

    private LocalDateTime createdAt;
}
