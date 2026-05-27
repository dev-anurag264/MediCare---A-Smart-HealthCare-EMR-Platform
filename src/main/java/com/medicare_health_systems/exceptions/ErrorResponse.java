package com.medicare_health_systems.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private int status;
    private String error;
    private String message;
    private String path;

    /**
     * For validation errors — maps field name to error message:
     * {
     *   "validationErrors": {
     *     "email": "Please provide a valid email address",
     *     "password": "Password must be between 8 and 100 characters"
     *   }
     * }
     */
    private Map<String, String> validationErrors;
}
