package org.example.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    @JsonProperty("sourceAirportId")
    private Long sourceAirportId;
    
    @JsonProperty("destinationAirportId")
    private Long destinationAirportId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("date")
    private LocalDate date;
    
    @JsonProperty("passengers")
    private Integer passengers;
    
    @JsonProperty("sortBy")
    private String sortBy;  // price, arrival, departure
    
    @JsonProperty("filters")
    private SearchFilters filters;
}

