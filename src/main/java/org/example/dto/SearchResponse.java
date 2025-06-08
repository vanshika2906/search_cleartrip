package org.example.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SearchResponse {
    private List<FlightPath> paths;
    private Integer totalCount;
    private Integer page;
    private Integer pageSize;
}

