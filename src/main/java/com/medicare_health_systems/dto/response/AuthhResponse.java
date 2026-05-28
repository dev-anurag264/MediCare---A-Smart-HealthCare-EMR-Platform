package com.medicare_health_systems.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthhResponse {

    private String message;

    private UserResponse user;
}