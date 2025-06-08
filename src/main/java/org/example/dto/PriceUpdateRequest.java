package org.example.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceUpdateRequest {
    private String flightId;
    private BigDecimal newPrice;
    private LocalDateTime timestamp;
} 