package org.example.controller;

import org.example.dto.*;
import org.example.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @PostMapping("/flights")
    public ResponseEntity<SearchResponse> searchFlights(@RequestBody SearchRequest request) {
        return ResponseEntity.ok(searchService.search(request));
    }
    
    @GetMapping("/flights/{flightNumber}")
    public ResponseEntity<FlightDetailsResponse> getFlightDetails(@PathVariable("flightNumber") String flightNumber) {
        return ResponseEntity.ok(searchService.getFlightDetails(flightNumber));
    }
    
    @PutMapping("/flights/price")
    public ResponseEntity<UpdateResponse> updatePrice(@RequestBody PriceUpdateRequest request) {
        return ResponseEntity.ok(searchService.updatePrice(request));
    }
    
    @PutMapping("/flights/seats")
    public ResponseEntity<UpdateResponse> updateSeats(@RequestBody SeatUpdateRequest request) {
        return ResponseEntity.ok(searchService.updateSeats(request));
    }
} 