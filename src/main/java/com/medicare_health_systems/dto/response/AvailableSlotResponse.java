package com.medicare_health_systems.dto.response;

import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotResponse {
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
}
