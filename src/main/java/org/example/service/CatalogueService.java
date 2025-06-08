package org.example.service;

import org.example.dto.catalogue.*;
import org.example.model.Airport;
import org.example.model.Flight;
import org.example.model.FlightStatus;
import org.example.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CatalogueService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private CacheService cacheService;

    @Transactional
    public String createFlightEntry(FlightEntryRequest request) {
        // Validate request
        validateFlightEntryRequest(request);

        // Create flight entry
        Flight flight = new Flight();
        flight.setFlightNumber(request.getFlightNumber());
        flight.setSourceAirport(getAirport(request.getSourceAirportId()));
        flight.setDestinationAirport(getAirport(request.getDestinationAirportId()));
        flight.setDepartureTime(LocalDateTime.of(LocalDate.now(), request.getDepartureTime()));
        flight.setTotalSeats(request.getTotalSeats());
        flight.setAvailableSeats(request.getTotalSeats());

        // Save flight
        flight = flightRepository.save(flight);

        // Invalidate search cache
        cacheService.invalidateSearchCache("*");

        return flight.getId();
    }

    public List<FlightScheduleResponse> getFlightSchedule(Long sourceAirportId, Long destinationAirportId,
                                                         LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Flight> flights = flightRepository.findBySourceAndDestinationAndDateRange(
            sourceAirportId, destinationAirportId, startDateTime, endDateTime);

        return flights.stream()
            .map(this::convertToScheduleResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void updateFlightEntry(String id, FlightEntryRequest request) {
        Flight flight = flightRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Flight entry not found"));

        // Update flight details
        flight.setDepartureTime(LocalDateTime.of(LocalDate.now(), request.getDepartureTime()));
        flight.setTotalSeats(request.getTotalSeats());
        flight.setAvailableSeats(request.getTotalSeats());

        // Save flight
        flightRepository.save(flight);

        // Invalidate caches
        cacheService.invalidateFlightCache(id);
        cacheService.invalidateSearchCache("*");
    }

    @Transactional
    public List<GeneratedFlightResponse> generateFlights(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Flight> flights = flightRepository.findByDateRange(startDateTime, endDateTime);
        
        List<GeneratedFlightResponse> generatedFlights = flights.stream()
            .map(this::convertToGeneratedFlightResponse)
            .collect(Collectors.toList());

        // Invalidate search cache
        cacheService.invalidateSearchCache("*");

        return generatedFlights;
    }

    @Transactional
    public void cancelFlight(String id, LocalDate date) {
        Flight flight = flightRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Flight not found"));

        // Update flight status
        flight.setStatus(FlightStatus.CANCELLED);

        // Save flight
        flightRepository.save(flight);

        // Invalidate caches
        cacheService.invalidateFlightCache(id);
        cacheService.invalidateSearchCache("*");
    }

    public List<GeneratedFlightResponse> getGeneratedFlights(LocalDate startDate, LocalDate endDate,
                                                           Long sourceAirportId, Long destinationAirportId) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Flight> flights = flightRepository.findByDateRangeAndAirports(
            startDateTime, endDateTime, sourceAirportId, destinationAirportId);

        return flights.stream()
            .map(this::convertToGeneratedFlightResponse)
            .collect(Collectors.toList());
    }

    private void validateFlightEntryRequest(FlightEntryRequest request) {
        if (request.getSourceAirportId() == null || request.getDestinationAirportId() == null) {
            throw new IllegalArgumentException("Source and destination airports are required");
        }
        if (request.getDepartureTime() == null) {
            throw new IllegalArgumentException("Departure time is required");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }
        if (request.getDaysAllowed() == null || request.getDaysAllowed().isEmpty()) {
            throw new IllegalArgumentException("Days allowed are required");
        }
        if (request.getFlightNumber() == null || request.getFlightNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Flight number is required");
        }
        if (request.getTotalSeats() == null || request.getTotalSeats() <= 0) {
            throw new IllegalArgumentException("Total seats must be greater than 0");
        }
    }

    private FlightScheduleResponse convertToScheduleResponse(Flight flight) {
        FlightScheduleResponse response = new FlightScheduleResponse();
        response.setFlightEntryId(flight.getId());
        response.setFlightNumber(flight.getFlightNumber());
        response.setDepartureTime(flight.getDepartureTime().toLocalTime());
        response.setTotalSeats(flight.getTotalSeats());
        // TODO: Set days allowed and cancelled dates
        return response;
    }

    private GeneratedFlightResponse convertToGeneratedFlightResponse(Flight flight) {
        GeneratedFlightResponse response = new GeneratedFlightResponse();
        response.setFlightId(flight.getId());
        response.setFlightNumber(flight.getFlightNumber());
        response.setDepartureTime(flight.getDepartureTime());
        response.setArrivalTime(flight.getArrivalTime());
        response.setSourceAirportId(flight.getSourceAirport().getId());
        response.setDestinationAirportId(flight.getDestinationAirport().getId());
        response.setTotalSeats(flight.getTotalSeats());
        response.setStatus(String.valueOf(flight.getStatus()));
        return response;
    }

    private Airport getAirport(Long id) {
        // TODO: Implement airport repository
        return new Airport();
    }
} 