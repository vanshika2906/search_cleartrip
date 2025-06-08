package org.example.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SeatUpdateRequest {
    private String flightId;
    private Integer availableSeats;
    private LocalDateTime timestamp;
} 