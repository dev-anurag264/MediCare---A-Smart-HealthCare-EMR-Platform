package com.medicare_health_systems.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AppointmentClashException extends RuntimeException{
    public AppointmentClashException(String message){
        super(message);
    }
}
