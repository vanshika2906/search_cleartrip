package org.example.service;

import org.example.dto.*;
import org.example.model.Airport;
import org.example.model.Flight;
import org.example.repository.FlightRepository;
import org.example.repository.AirportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    
    @Autowired
    private PathFindingService pathFindingService;
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private AirportRepository airportRepository;
    
    //@Cacheable(value = "searchCache", key = "#p0.sourceAirportId + ':' + #p0.destinationAirportId + ':' + #p0.date")
    public SearchResponse search(SearchRequest request) {
        // Get source and destination airports
        System.out.println("Processing request: " + request);
        Airport source = getAirport(request.getSourceAirportId());
        Airport destination = getAirport(request.getDestinationAirportId());
        
        // Convert date to LocalDateTime for the start of the day
        LocalDateTime searchDate = request.getDate().atStartOfDay();
        
        // Find all possible paths
        List<List<Flight>> paths = pathFindingService.findPaths(
            source, destination, searchDate, getMaxStops(request.getFilters())
        );
        
        // Convert paths to DTOs and enrich with latest price and seat data
        List<FlightPath> flightPaths = paths.stream()
            .map(this::convertToFlightPath)
            .collect(Collectors.toList());
        
        // Apply filters
        flightPaths = applyFilters(flightPaths, request.getFilters());
        
        // Apply sorting
        if (request.getSortBy() != null) {
            sortPaths(flightPaths, request.getSortBy());
        }
        
        // Create response
        SearchResponse response = new SearchResponse();
        response.setPaths(flightPaths);
        response.setTotalCount(flightPaths.size());
        response.setPage(1); // TODO: Implement pagination
        response.setPageSize(flightPaths.size());

        System.out.println("Processing2");
        
        return response;
    }
    
    //@Cacheable(value = "flightCache", key = "#id", unless = "#result == null")
    public FlightDetailsResponse getFlightDetails(String id) {
        Flight flight = flightRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Flight not found"));
            
        FlightDetailsResponse response = convertToFlightDetails(flight);
        
        // Enrich with latest price and seat data
        BigDecimal cachedPrice = cacheService.getCachedPrice(id);
        if (cachedPrice != null) {
            response.setPrice(cachedPrice);
        }
        
        Integer cachedSeats = cacheService.getCachedSeats(id);
        if (cachedSeats != null) {
            response.setAvailableSeats(cachedSeats);
        }
        
        return response;
    }
    
    @Transactional
    //@CacheEvict(value = "priceCache", key = "#request.flightId")
    public UpdateResponse updatePrice(PriceUpdateRequest request) {
        Flight flight = flightRepository.findById(request.getFlightId())
            .orElseThrow(() -> new RuntimeException("Flight not found"));
            
        flight.setPrice(request.getNewPrice());
        flightRepository.save(flight);
        
        // Update price cache
        cacheService.invalidatePriceCache(request.getFlightId());
        
        UpdateResponse response = new UpdateResponse();
        response.setSuccess(true);
        response.setMessage("Price updated successfully");
        return response;
    }
    
    @Transactional
    //@CacheEvict(value = "seatCache", key = "#request.flightId")
    public UpdateResponse updateSeats(SeatUpdateRequest request) {
        Flight flight = flightRepository.findById(request.getFlightId())
            .orElseThrow(() -> new RuntimeException("Flight not found"));
            
        if (request.getAvailableSeats() > flight.getTotalSeats()) {
            UpdateResponse response = new UpdateResponse();
            response.setSuccess(false);
            response.setMessage("Available seats cannot be greater than total seats");
            return response;
        }
        
        flight.setAvailableSeats(request.getAvailableSeats());
        flightRepository.save(flight);
        
        // Update seat cache
        cacheService.invalidateSeatCache(request.getFlightId());
        
        UpdateResponse response = new UpdateResponse();
        response.setSuccess(true);
        response.setMessage("Seats updated successfully");
        return response;
    }
    
    @Transactional
    //@CacheEvict(value = "searchCache", allEntries = true)
    public FlightDetailsResponse createFlight(FlightCreateRequest request) {
        // Validate request
        if (request.getAvailableSeats() > request.getTotalSeats()) {
            throw new IllegalArgumentException("Available seats cannot be greater than total seats");
        }

        // Get source and destination airports
        Airport sourceAirport = getAirport(request.getSourceAirportId());
        Airport destAirport = getAirport(request.getDestinationAirportId());

        // Create new flight
        Flight flight = new Flight();
        flight.setFlightNumber(request.getFlightNumber());
        flight.setSourceAirport(sourceAirport);
        flight.setDestinationAirport(destAirport);
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setPrice(request.getPrice());
        flight.setTotalSeats(request.getTotalSeats());
        flight.setAvailableSeats(request.getAvailableSeats());

        // Save flight
        flight = flightRepository.save(flight);

        // Update price and seat caches
        cacheService.setCachedPrice(flight.getId(), flight.getPrice());
        cacheService.setCachedSeats(flight.getId(), flight.getAvailableSeats());

        // Convert to response
        return convertToFlightDetails(flight);
    }
    
    private FlightPath convertToFlightPath(List<Flight> flights) {
        FlightPath path = new FlightPath();
        path.setFlights(flights.stream()
            .map(this::convertToFlightInfo)
            .collect(Collectors.toList()));
        
        // Calculate total price using cached prices if available
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Flight flight : flights) {
            BigDecimal cachedPrice = cacheService.getCachedPrice(flight.getId());
            totalPrice = totalPrice.add(cachedPrice != null ? cachedPrice : flight.getPrice());
        }
        path.setTotalPrice(totalPrice);
        
        // Calculate total stops
        path.setTotalStops(flights.size() - 1);
        
        // Calculate total duration
        if (!flights.isEmpty()) {
            Flight first = flights.get(0);
            Flight last = flights.get(flights.size() - 1);
            path.setTotalDuration(last.getArrivalTime().minusMinutes(
                first.getDepartureTime().toLocalTime().toSecondOfDay() / 60
            ));
        }
        
        return path;
    }
    
    private FlightInfo convertToFlightInfo(Flight flight) {
        FlightInfo info = new FlightInfo();
        info.setId(flight.getId());
        info.setFlightNumber(flight.getFlightNumber());
        info.setAirline(flight.getFlightNumber().substring(0, 2));
        
        // Use cached price if available
        BigDecimal cachedPrice = cacheService.getCachedPrice(flight.getId());
        info.setPrice(cachedPrice != null ? cachedPrice : flight.getPrice());
        
        info.setDepartureTime(flight.getDepartureTime());
        info.setArrivalTime(flight.getArrivalTime());
        
        // Use cached seats if available
        Integer cachedSeats = cacheService.getCachedSeats(flight.getId());
        info.setAvailableSeats(cachedSeats != null ? cachedSeats : flight.getAvailableSeats());
        
        return info;
    }
    
    private List<FlightPath> applyFilters(List<FlightPath> paths, SearchFilters filters) {
        if (filters == null) {
            return paths;
        }
        
        return paths.stream()
            .filter(path -> filterByPrice(path, filters.getPriceRange()))
            .filter(path -> filterByAirlines(path, filters.getAirlines()))
            .collect(Collectors.toList());
    }
    
    private boolean filterByPrice(FlightPath path, PriceRange range) {
        if (range == null) {
            return true;
        }
        
        return path.getTotalPrice().compareTo(BigDecimal.valueOf(range.getMinPrice())) >= 0 &&
               path.getTotalPrice().compareTo(BigDecimal.valueOf(range.getMaxPrice())) <= 0;
    }
    
    private boolean filterByAirlines(FlightPath path, Long[] airlines) {
        if (airlines == null || airlines.length == 0) {
            return true;
        }
        
        Set<Long> airlineSet = new HashSet<>(Arrays.asList(airlines));
        return path.getFlights().stream()
            .anyMatch(flight -> airlineSet.contains(Long.parseLong(flight.getAirline())));
    }
    
    private void sortPaths(List<FlightPath> paths, String sortBy) {
        switch (sortBy) {
            case "price":
                paths.sort(Comparator.comparing(FlightPath::getTotalPrice));
                break;
            case "arrival":
                paths.sort(Comparator.comparing(path -> 
                    path.getFlights().get(path.getFlights().size() - 1).getArrivalTime()));
                break;
            case "departure":
                paths.sort(Comparator.comparing(path -> 
                    path.getFlights().get(0).getDepartureTime()));
                break;
        }
    }
    
    private FlightDetailsResponse convertToFlightDetails(Flight flight) {
        FlightDetailsResponse response = new FlightDetailsResponse();
        response.setId(flight.getId());
        response.setFlightNumber(flight.getFlightNumber());
        response.setAirline(flight.getFlightNumber().substring(0, 2));
        response.setPrice(flight.getPrice());
        response.setDepartureTime(flight.getDepartureTime());
        response.setArrivalTime(flight.getArrivalTime());
        response.setAvailableSeats(flight.getAvailableSeats());
        response.setTotalSeats(flight.getTotalSeats());
        
        // Set source airport info
        AirportInfo sourceAirport = new AirportInfo();
        sourceAirport.setId(flight.getSourceAirport().getId());
        sourceAirport.setName(flight.getSourceAirport().getName());
        sourceAirport.setCode(flight.getSourceAirport().getCode());
        response.setSourceAirport(sourceAirport);
        
        // Set destination airport info
        AirportInfo destAirport = new AirportInfo();
        destAirport.setId(flight.getDestinationAirport().getId());
        destAirport.setName(flight.getDestinationAirport().getName());
        destAirport.setCode(flight.getDestinationAirport().getCode());
        response.setDestinationAirport(destAirport);
        
        return response;
    }
    
    private int getMaxStops(SearchFilters filters) {
        if (filters == null || filters.getStops() == null) {
            return 2; // Default max stops
        }
        
        switch (filters.getStops()) {
            case "direct":
                return 0;
            case "one_stop":
                return 1;
            case "multiple_stops":
                return 2;
            default:
                return 2;
        }
    }
    
    private List<Airport> getAllAirports() {
        // TODO: Implement airport repository
        return new ArrayList<>();
    }
    
    private Airport getAirport(Long id) {
        return airportRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Airport not found with id: " + id));
    }
} 