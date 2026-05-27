package com.medicare_health_systems.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFound extends RuntimeException {
    public ResourceNotFound(String message) {
        super(message);
    }
    public ResourceNotFound(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("% not found with %s: '%s'",resourceName,fieldName, fieldValue));
    }

}
