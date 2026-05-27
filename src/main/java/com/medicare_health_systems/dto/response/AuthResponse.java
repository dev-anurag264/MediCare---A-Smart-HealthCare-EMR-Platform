package com.medicare_health_systems.dto.response;

import com.medicare_health_systems.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuthResponse {

    private String token; // The JWT - client stores this and sends in every subsequent request

    @Builder.Default
    private String tokenType = "Bearer"; //Always "Bearer" per OAuth2/JWT convention.
                                        // Client formats the header as: "Authorization: Bearer <token>"

    private Long expiresIn;

    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}
