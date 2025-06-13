package org.example.controller;
import org.example.dto.*;
import org.example.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

//TODO: Add LLM call to fetch the keywords and could better vector search
@RestController
@RequestMapping("/api/v1/search")
public class ChatController {

    @Autowired
    private SearchService searchService;

    @PostMapping("/chat")
    public ResponseEntity<Object> handleChat(@RequestBody Map<String, String> body) {
        String message = body.get("message").toLowerCase();

        try {
            if (message.contains("flight") && message.contains("from") && message.contains("to")) {
                // Assume it's a search
                Map<String, Object> extracted = extractSearchParams(message);
                SearchRequest req = new SearchRequest();
                req.setSourceAirportId((Long) extracted.get("sourceId"));
                req.setDestinationAirportId((Long) extracted.get("destId"));
                req.setDate((LocalDate) extracted.get("date"));
                req.setPassengers(1);
                req.setSortBy("price");

                return ResponseEntity.ok(searchService.search(req));
            } else if (message.contains("details") || message.contains("info")) {
                // Assume it's a flight details query
                String flightNumber = extractFlightNumber(message);
                return ResponseEntity.ok(searchService.getFlightDetails(flightNumber));
            } else {
                return ResponseEntity.badRequest().body("Sorry, I couldn't understand the request.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    private Map<String, Object> extractSearchParams(String msg) {
        Map<String, Object> map = new HashMap<>();

        // Mock mapping for demo
        Map<String, Long> cityToId = Map.of(
                "delhi", 1L,
                "mumbai", 2L,
                "bangalore", 3L
        );

        Long source = cityToId.entrySet().stream()
                .filter(e -> msg.contains("from " + e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst().orElseThrow(() -> new RuntimeException("Source not found"));

        Long dest = cityToId.entrySet().stream()
                .filter(e -> msg.contains("to " + e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst().orElseThrow(() -> new RuntimeException("Destination not found"));

        // Basic date handling
        LocalDate date = LocalDate.now(); // default
        if (msg.contains("april 1")) {
            date = LocalDate.of(2024, 4, 1);
        }

        map.put("sourceId", source);
        map.put("destId", dest);
        map.put("date", date);
        return map;
    }

    private String extractFlightNumber(String msg) {
        return msg.toUpperCase().replaceAll("[^A-Z0-9]", "").replace("FLIGHT", "").trim();
    }
}
