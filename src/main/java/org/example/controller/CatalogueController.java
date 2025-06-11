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
            @RequestParam("sourceAirportId") Long sourceAirportId,
            @RequestParam("destinationAirportId") Long destinationAirportId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(catalogueService.getFlightSchedule(
            sourceAirportId, destinationAirportId, startDate, endDate));
    }

    @PutMapping("/flight-entry/{flightNumber}")
    public ResponseEntity<Map<String, String>> updateFlightEntry(
            @PathVariable("flightNumber") String flightNumber,
            @RequestBody FlightEntryRequest request) {
        try {
            catalogueService.updateFlightEntry(flightNumber, request);
            return ResponseEntity.ok(Map.of(
                "success", "true",
                "message", "Flight entries updated successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", "false",
                "message", e.getMessage()
            ));
        }
    }


    @PostMapping("/flight-entry/{flightNumber}/cancel")
    public ResponseEntity<Map<String, String>> cancelFlight(
            @PathVariable("flightNumber") String flightNumber,
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            catalogueService.cancelFlight(flightNumber, date);
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

    @DeleteMapping("/flight-entry/{flightNumber}")
    public ResponseEntity<Map<String, String>> deleteFlightEntries(
            @PathVariable("flightNumber") String flightNumber) {
        try {
            catalogueService.deleteFlightEntries(flightNumber);
            return ResponseEntity.ok(Map.of(
                "success", "true",
                "message", "Flight entries deleted successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", "false",
                "message", e.getMessage()
            ));
        }
    }

} 