package org.example.service;

import org.example.dto.catalogue.*;
import org.example.model.Airport;
import org.example.model.Flight;
import org.example.model.FlightStatus;
import org.example.repository.AirportRepository;
import org.example.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
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

    @Autowired
    private AirportRepository airportRepository;

    @Transactional
    public String createFlightEntry(FlightEntryRequest request) {
        // Validate request
        validateFlightEntryRequest(request);

        // Get all dates between start and end date that match the allowed days
        List<LocalDate> flightDates = generateFlightDates(
            request.getStartDate(),
            request.getEndDate(),
            request.getDaysAllowed()
        );

        // Create flight entries for each date
        List<Flight> flights = new ArrayList<>();
        Airport source = getAirport(request.getSourceAirportId());
        Airport destination = getAirport(request.getDestinationAirportId());
        for (LocalDate date : flightDates) {
            Flight flight = new Flight();
            flight.setFlightNumber(request.getFlightNumber());
            flight.setSourceAirport(source);
            flight.setDestinationAirport(destination);
            
            // Set departure time for the specific date
            LocalDateTime departureTime = LocalDateTime.of(date, request.getDepartureTime());
            flight.setDepartureTime(departureTime);
            
            // Calculate arrival time (assuming 3 hours flight duration for this example)
            LocalDateTime arrivalTime = departureTime.plusHours(3);
            flight.setArrivalTime(arrivalTime);
            
            flight.setFlightDate(date);
            flight.setTotalSeats(request.getTotalSeats());
            flight.setAvailableSeats(request.getTotalSeats());
            flight.setStatus(FlightStatus.SCHEDULED);


            flight.setPrice(request.getPrice());
            
            // Set a unique ID for each flight
            flight.setId(request.getFlightNumber() + "_" + date.toString());
            
            flights.add(flight);
        }

        // Save all flights
        List<Flight> savedFlights = flightRepository.saveAll(flights);

        // Invalidate search cache
        cacheService.invalidateSearchCache("*");

        // Return the first flight's ID as reference
        return savedFlights.get(0).getId();
    }

    private List<LocalDate> generateFlightDates(LocalDate startDate, LocalDate endDate, List<String> daysAllowed) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        // Convert days to DayOfWeek enum values
        Set<DayOfWeek> allowedDays = daysAllowed.stream()
            .map(day -> DayOfWeek.valueOf(day.toUpperCase()))
            .collect(Collectors.toSet());
        
        while (!currentDate.isAfter(endDate)) {
            if (allowedDays.contains(currentDate.getDayOfWeek())) {
                dates.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return dates;
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
    public void updateFlightEntry(String flightNumber, FlightEntryRequest request) {
        // Validate request
        validateFlightEntryRequest(request);

        // Get all existing flights with this flight number
        List<Flight> existingFlights = flightRepository.findByFlightNumber(flightNumber);
        if (existingFlights.isEmpty()) {
            throw new RuntimeException("No flights found with number: " + flightNumber);
        }

        // Get all dates between start and end date that match the allowed days
        List<LocalDate> flightDates = generateFlightDates(
            request.getStartDate(),
            request.getEndDate(),
            request.getDaysAllowed()
        );

        // Create a map of existing flights by date
        Map<LocalDate, Flight> existingFlightsByDate = existingFlights.stream()
            .collect(Collectors.toMap(Flight::getFlightDate, flight -> flight));

        // Create or update flight entries for each date
        List<Flight> flightsToSave = new ArrayList<>();
        Airport source = getAirport(request.getSourceAirportId());
        Airport destination = getAirport(request.getDestinationAirportId());

        for (LocalDate date : flightDates) {
            Flight flight;
            if (existingFlightsByDate.containsKey(date)) {
                // Update existing flight
                flight = existingFlightsByDate.get(date);
            } else {
                continue;
            }

            // Update common fields
            flight.setSourceAirport(source);
            flight.setDestinationAirport(destination);
            LocalDateTime departureTime = LocalDateTime.of(date, request.getDepartureTime());
            flight.setDepartureTime(departureTime);
            flight.setArrivalTime(departureTime.plusHours(3)); // Assuming 3 hours flight duration
            flight.setFlightDate(date);
            flight.setTotalSeats(!Objects.isNull(request.getTotalSeats()) ? request.getTotalSeats() : flight.getTotalSeats());
            flight.setPrice(!Objects.isNull(request.getPrice()) ? request.getPrice() : flight.getPrice());

            flightsToSave.add(flight);
        }

        // Save all flights
        flightRepository.saveAll(flightsToSave);

        // Invalidate caches
        cacheService.invalidateSearchCache("*");
    }


    @Transactional
    public void cancelFlight(String flightNumber, LocalDate date) {
        // Get all flights with this flight number
        List<Flight> existingFlights = flightRepository.findByFlightNumber(flightNumber);
        if (existingFlights.isEmpty()) {
            throw new RuntimeException("No flights found with number: " + flightNumber);
        }

        // Find the specific flight for the given date
        Flight flightToCancel = existingFlights.stream()
            .filter(flight -> flight.getFlightDate().equals(date))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No flight found with number " + flightNumber + " on date " + date));

        // Update flight status
        flightToCancel.setStatus(FlightStatus.CANCELLED);

        // Save flight
        flightRepository.save(flightToCancel);

        // Invalidate caches
        cacheService.invalidateSearchCache("*");
    }


    @Transactional
    public void deleteFlightEntries(String flightNumber) {
        // Get all flights with this flight number
        List<Flight> flights = flightRepository.findByFlightNumber(flightNumber);
        if (flights.isEmpty()) {
            throw new RuntimeException("No flights found with number: " + flightNumber);
        }

        // Delete all flights
        flightRepository.deleteAll(flights);

        // Invalidate caches
        cacheService.invalidateSearchCache("*");
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
        return airportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Airport not found with id: " + id));
    }
} 