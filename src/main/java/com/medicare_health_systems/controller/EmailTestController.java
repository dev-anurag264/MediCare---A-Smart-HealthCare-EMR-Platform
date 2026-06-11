package com.medicare_health_systems.controller;

import com.medicare_health_systems.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;


@RestController
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    @GetMapping("/test-email")
    public String testEmail() {

        Context context = new Context();
        context.setVariable("name", "Anurag");

        emailService.sendHtmlEmail(
                "prasad.anurag786@gmail.com",
                "Test Mail",
                "email/appointment-booked",
                context
        );

        return "sent";
    }
}