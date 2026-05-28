package com.medicare_health_systems.config;

import com.medicare_health_systems.utils.AppConstants;
import com.medicare_health_systems.utils.JwtAuthenticationFilter;
import com.medicare_health_systems.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configure(http))

                // ─── SESSION: No server-side sessions — JWT handles state ──────
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ─── AUTHORIZATION RULES ──────────────────────────────────────
                // Rules are evaluated IN ORDER — first match wins.
                // Most specific rules MUST come before more general ones.
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints — no token required
                        // permitAll() = anyone, authenticated or not
                        .requestMatchers(AppConstants.PUBLIC_ENDPOINTS).permitAll()

                        // HTTP OPTIONS requests for CORS preflight — always allow
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Admin-only endpoints
                        .requestMatchers(AppConstants.ADMIN_PATH + "/**")
                        .hasRole(AppConstants.ROLE_ADMIN)

                        // Doctor and Admin can access doctor-specific endpoints
                        // hasAnyRole: user must have AT LEAST ONE of the listed roles
                        .requestMatchers(AppConstants.DOCTORS_PATH + "/**")
                        .hasAnyRole(AppConstants.ROLE_DOCTOR, AppConstants.ROLE_ADMIN)

                        // Patient and Admin for patient-specific endpoints
                        .requestMatchers(AppConstants.PATIENTS_PATH + "/**")
                        .hasAnyRole(AppConstants.ROLE_PATIENT, AppConstants.ROLE_ADMIN)

                        // All other requests: must be authenticated (any role)
                        // authenticated() = logged in with any role
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return  http.build();
    }
}
