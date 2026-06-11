package com.medicare_health_systems.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AppointmentCancelledEvent extends ApplicationEvent {
    private final Long appointmentId;
    private final String patientEmail;
    private final String patientName;
    private final String doctorEmail;
    private final String doctorName;
    private final String appointmentDate;
    private final String startTime;
    private final String cancelledBy; // "PATIENT", "DOCTOR", or "ADMIN"

    public AppointmentCancelledEvent(Object source, Long appointmentId,
                                     String patientEmail, String patientName,
                                     String doctorEmail, String doctorName,
                                     String appointmentDate, String startTime, String cancelledBy) {
        super(source);
        this.appointmentId = appointmentId;
        this.patientEmail = patientEmail;
        this.patientName = patientName;
        this.doctorEmail = doctorEmail;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.cancelledBy = cancelledBy;
    }
}
