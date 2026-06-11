package com.medicare_health_systems.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MedicalRecordCreatedEvent extends ApplicationEvent {

    private final Long medicalRecord;
    private final String patientEmail;
    private final String patientName;
    private final String doctorName;
    private final String visitDate;

    public MedicalRecordCreatedEvent(Object source, Long medicalRecord, String patientEmail, String patientName, String doctorName, String visitDate) {
        super(source);
        this.medicalRecord = medicalRecord;
        this.patientEmail = patientEmail;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.visitDate = visitDate;
    }
}
