package org.example.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilters {
    private String stops;  // direct, one_stop, multiple_stops
    private PriceRange priceRange;
    private Long[] airlines;
}
