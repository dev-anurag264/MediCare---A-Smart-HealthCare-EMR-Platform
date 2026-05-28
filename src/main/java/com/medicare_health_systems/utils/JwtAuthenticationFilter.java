package com.medicare_health_systems.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NotNull  HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(AppConstants.JWT_HEADER);

        if (authHeader == null || !authHeader.startsWith(AppConstants.JWT_TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(AppConstants.JWT_PREFIX_LENGTH);
        log.debug("Processing JWT token for request: {}", request.getRequestURI());


        final String userEmail;
        try {
            userEmail = jwtUtil.extractUsername(jwt);
        } catch (Exception ex) {

            log.warn("JWT token processing failed: {} | Path: {}", ex.getMessage(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }


        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // STEP 5: Validate token
            if (jwtUtil.isTokenValid(jwt, userDetails)) {


                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Attach request details (IP address, session ID) for audit logging
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // STEP 6: Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Authentication set for user: {} | Role: {}",
                        userEmail, userDetails.getAuthorities());
            } else {
                log.warn("JWT token validation failed for user: {}", userEmail);
            }
        }

        // STEP 7: Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
