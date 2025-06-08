package org.example.controller;

import org.example.dto.*;
import org.example.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    
    @Autowired
    private SearchService searchService;
    
    @PostMapping("/flights")
    public ResponseEntity<SearchResponse> searchFlights(@RequestBody SearchRequest request) {
        return ResponseEntity.ok(searchService.search(request));
    }
    
    @GetMapping("/flights/{id}")
    public ResponseEntity<FlightDetailsResponse> getFlightDetails(@PathVariable String id) {
        return ResponseEntity.ok(searchService.getFlightDetails(id));
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