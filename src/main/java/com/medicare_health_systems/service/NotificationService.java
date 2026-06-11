package com.medicare_health_systems.service;

import com.medicare_health_systems.events.AppointmentBookedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;

    @EventListener
    public void handleAppointmentBooked(AppointmentBookedEvent event) {
        log.info("Handling AppointmentBookedEvent for appointment: {}", event.getAppointmentId());

        emailService.sendAppointmentBookedEmail(
                event.getPatientEmail(),
                event.getPatientName(),
                event.getDoctorName(),
                event.getAppointmentDate(),
                event.getStartTime(),
                event.getAppointmentId()
        );
    }

}
