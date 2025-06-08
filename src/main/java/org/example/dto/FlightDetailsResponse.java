package org.example.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FlightDetailsResponse {
    private String id;
    private String flightNumber;
    private String airline;
    private BigDecimal price;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private AirportInfo sourceAirport;
    private AirportInfo destinationAirport;
    private Integer stops;
    private Integer availableSeats;
    private Integer totalSeats;
}

