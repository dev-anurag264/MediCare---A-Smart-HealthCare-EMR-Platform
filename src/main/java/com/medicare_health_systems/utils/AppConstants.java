package com.medicare_health_systems.utils;

public final class AppConstants {

    // Prevent instantiation — utility class
    private AppConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ─────────────────────────────────────────────────────────────
    // API PATHS
    // ─────────────────────────────────────────────────────────────
    public static final String API_BASE_PATH    = "/api/v1";
    public static final String AUTH_PATH        = API_BASE_PATH + "/auth";
    public static final String USERS_PATH       = API_BASE_PATH + "/users";
    public static final String DOCTORS_PATH     = API_BASE_PATH + "/doctors";
    public static final String PATIENTS_PATH    = API_BASE_PATH + "/patients";
    public static final String ADMIN_PATH       = API_BASE_PATH + "/admin";


    // PUBLIC ENDPOINTS (no authentication required)
    // Listed here for SecurityConfig to permit without token
    //
    public static final String[] PUBLIC_ENDPOINTS = {
            AUTH_PATH + "/**",          // /api/v1/auth/register, /api/v1/auth/login
            "/swagger-ui/**",           // Swagger UI HTML
            "/swagger-ui.html",
            "/api-docs/**",             // OpenAPI JSON spec
            "/v3/api-docs/**",          // OpenAPI v3
            "/actuator/health"          // Health check (for load balancers)
    };

    // ─────────────────────────────────────────────────────────────
    // ROLES
    // ─────────────────────────────────────────────────────────────
    public static final String ROLE_PATIENT = "PATIENT";
    public static final String ROLE_DOCTOR  = "DOCTOR";
    public static final String ROLE_ADMIN   = "ADMIN";

    // ─────────────────────────────────────────────────────────────
    // JWT
    // ─────────────────────────────────────────────────────────────
    public static final String JWT_HEADER        = "Authorization";
    public static final String JWT_TOKEN_PREFIX  = "Bearer ";
    public static final int    JWT_PREFIX_LENGTH = 7; // "Bearer ".length()

    // ─────────────────────────────────────────────────────────────
    // PAGINATION DEFAULTS
    // ─────────────────────────────────────────────────────────────
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE   = 10;
    public static final int MAX_PAGE_SIZE       = 100;
    public static final String DEFAULT_SORT_BY  = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";
}
