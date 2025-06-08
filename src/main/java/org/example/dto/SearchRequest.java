package org.example.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SearchRequest {
    private Long sourceAirportId;
    private Long destinationAirportId;
    private LocalDate date;
    private Integer passengers;
    private String sortBy;  // price, arrival, departure
    private SearchFilters filters;
}

