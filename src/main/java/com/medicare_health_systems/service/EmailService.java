package com.medicare_health_systems.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;



@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Value("${application.mail.from-name:MediCare HealthCare}")
    private String fromName;

    //appointment booked
    @Async("emailTaskExecutor")
    public void sendAppointmentBookedEmail(
            String toEmail,
            String patientName,
            String doctorName,
            String appointmentDate,
            String startTime,
            Long appointmentId
    ){
        Context context = new Context();
        context.setVariable("patientName", patientName);
        context.setVariable("doctorName", doctorName);
        context.setVariable("appointmentDate", appointmentDate);
        context.setVariable("startTime", startTime);
        context.setVariable("appointmentId", appointmentId);

        sendHtmlEmail(
                toEmail,
                "Appointment Confirmation - Medicare Health",
                "email/appointment-booked",
                context
        );
    }
    //appointment confirmed
    @Async("emailTaskExecutor")
    public void sendAppointmentConfirmedEmail(String toEmail, String patientName,
                                              String doctorName, String appointmentDate, String startTime) {

        Context context = new Context();
        context.setVariable("patientName", patientName);
        context.setVariable("doctorName", doctorName);
        context.setVariable("appointmentDate", appointmentDate);
        context.setVariable("startTime", startTime);

        sendHtmlEmail(
                toEmail,
                "Appointment Confirmed - Healthcare Platform",
                "email/appointment-confirmed",
                context
        );
    }
    //medical record created email
    @Async("emailTaskExecutor")
    public void sendMedicalRecordCreatedEmail(String toEmail, String patientName,
                                              String doctorName, String visitDate, Long recordId) {

        Context context = new Context();
        context.setVariable("patientName", patientName);
        context.setVariable("doctorName", doctorName);
        context.setVariable("visitDate", visitDate);
        context.setVariable("recordId", recordId);

        sendHtmlEmail(
                toEmail,
                "Medical Record Available - Healthcare Platform",
                "email/medical-record-created",
                context
        );
    }

    //appointment cancelled
    @Async("emailTaskExecutor")
    public void sendAppointmentCancelledEmail(String toEmail, String recipientName,
                                              String doctorName, String patientName,
                                              String appointmentDate, String startTime, String cancelledBy) {

        Context context = new Context();
        context.setVariable("recipientName", recipientName);
        context.setVariable("doctorName", doctorName);
        context.setVariable("patientName", patientName);
        context.setVariable("appointmentDate", appointmentDate);
        context.setVariable("startTime", startTime);
        context.setVariable("cancelledBy", cancelledBy);

        sendHtmlEmail(
                toEmail,
                "Appointment Cancelled - Healthcare Platform",
                "email/appointment-cancelled",
                context
        );
    }

    public void sendHtmlEmail(String toEmail, String subject, String templateName, Context context){
        try{
            String htmlContent = templateEngine.process(templateName, context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = isHtml

            mailSender.send(message);
            log.info("Email sent successfully to: {} | Subject: {}", toEmail, subject);
        }catch (Exception e){
            log.error(
                    "Failed to send email to: {} | Subject: {}",
                    toEmail,
                    subject,
                    e
            );
        }
    }
}
