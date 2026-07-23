package com.medicare_health_systems.kafka;

//message dto

import lombok.*;

import java.io.Serializable;
//kafka ---> Java Object ---> Serialized

public class KafkaMessages {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentBookedMessage implements Serializable {
        private Long appointmentId;
        private String patientEmail;
        private String patientName;
        private String doctorName;
        private String appointmentDate;
        private String startTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentConfirmedMessage implements Serializable {
        private Long appointmentId;
        private String patientEmail;
        private String patientName;
        private String doctorName;
        private String appointmentDate;
        private String startTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentCancelledMessage implements Serializable {
        private Long appointmentId;
        private String patientEmail;
        private String patientName;
        private String doctorEmail;
        private String doctorName;
        private String appointmentDate;
        private String startTime;
        private String cancelledBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicalRecordCreatedMessage implements Serializable {
        private Long medicalRecordId;
        private String patientEmail;
        private String patientName;
        private String doctorName;
        private String visitDate;
    }
}
