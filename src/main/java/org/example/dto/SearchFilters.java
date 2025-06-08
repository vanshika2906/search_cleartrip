package org.example.dto;

import lombok.Data;

@Data
public class SearchFilters {
    private String stops;  // direct, one_stop, multiple_stops
    private PriceRange priceRange;
    private Long[] airlines;
}
