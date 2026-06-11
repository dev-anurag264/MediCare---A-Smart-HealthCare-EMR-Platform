package com.medicare_health_systems.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AppointmentBookedEvent extends ApplicationEvent {

    private final Long appointmentId;
    private final String patientEmail;
    private final String patientName;
    private final String doctorName;
    private final String appointmentDate;
    private final String startTime;

    public AppointmentBookedEvent(Object source, Long appointmentId,
                                  String patientEmail, String patientName,
                                  String doctorName, String appointmentDate, String startTime) {
        super(source);
        this.appointmentId = appointmentId;
        this.patientEmail = patientEmail;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
    }


}
