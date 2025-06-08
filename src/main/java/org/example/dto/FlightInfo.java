package org.example.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FlightInfo {
    private String id;
    private String flightNumber;
    private String airline;
    private BigDecimal price;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer stops;
    private Integer availableSeats;
}
