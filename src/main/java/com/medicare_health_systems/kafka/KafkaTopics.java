package com.medicare_health_systems.kafka;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String APPOINTMENT_BOOKED     = "appointment.booked";
    public static final String APPOINTMENT_CONFIRMED  = "appointment.confirmed";
    public static final String APPOINTMENT_CANCELLED  = "appointment.cancelled";
    public static final String MEDICAL_RECORD_CREATED = "medical-record.created";
}
