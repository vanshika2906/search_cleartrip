package org.example.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FlightPath {
    private List<FlightInfo> flights;
    private BigDecimal totalPrice;
    private Integer totalStops;
    private LocalDateTime totalDuration;
}
