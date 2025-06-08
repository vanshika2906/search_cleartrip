package org.example.controller;

import org.example.dto.catalogue.*;
import org.example.service.CatalogueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalogue")
public class CatalogueController {

    @Autowired
    private CatalogueService catalogueService;

    @PostMapping("/flight-entry")
    public ResponseEntity<Map<String, String>> createFlightEntry(@RequestBody FlightEntryRequest request) {
        try {
            String flightEntryId = catalogueService.createFlightEntry(request);
            return ResponseEntity.ok(Map.of(
                "success", "true",
                "message", "Flight entry created successfully",
                "flight_entry_id", flightEntryId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", "false",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/flight-schedule")
    public ResponseEntity<List<FlightScheduleResponse>> getFlightSchedule(
            @RequestParam Long sourceAirportId,
            @RequestParam Long destinationAirportId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(catalogueService.getFlightSchedule(
            sourceAirportId, destinationAirportId, startDate, endDate));
    }

    @PutMapping("/flight-entry/{id}")
    public ResponseEntity<Map<String, String>> updateFlightEntry(
            @PathVariable String id,
            @RequestBody FlightEntryRequest request) {
        try {
            catalogueService.updateFlightEntry(id, request);
            return ResponseEntity.ok(Map.of(
                "success", "true",
                "message", "Flight entry updated successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", "false",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/generate-flights")
    public ResponseEntity<Map<String, Object>> generateFlights(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<GeneratedFlightResponse> generatedFlights = catalogueService.generateFlights(startDate, endDate);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Flights generated successfully",
                "generated_flights", generatedFlights
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/flight-entry/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancelFlight(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            catalogueService.cancelFlight(id, date);
            return ResponseEntity.ok(Map.of(
                "success", "true",
                "message", "Flight cancelled successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", "false",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/generated-flights")
    public ResponseEntity<List<GeneratedFlightResponse>> getGeneratedFlights(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long sourceAirportId,
            @RequestParam(required = false) Long destinationAirportId) {
        return ResponseEntity.ok(catalogueService.getGeneratedFlights(
            startDate, endDate, sourceAirportId, destinationAirportId));
    }
} 