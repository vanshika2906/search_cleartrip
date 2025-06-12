package org.example.service;

import org.example.dto.*;
import org.example.model.Airport;
import org.example.model.Flight;
import org.example.dto.FlightDetails;
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

    public SearchResponse search(SearchRequest request) {

        FlightDetails flightDetail = getFlightPaths(request);

        List<FlightPath> flightPaths = flightDetail.getFlightPath().stream()
            .map(this::convertToFlightPath)
            .collect(Collectors.toList());

        flightPaths = applyFilters(flightPaths, request.getFilters());

        if (request.getSortBy() != null) {
            sortPaths(flightPaths, request.getSortBy());
        }

        SearchResponse response = new SearchResponse();
        response.setPaths(flightPaths);
        response.setTotalCount(flightPaths.size());
        response.setPage(1); // TODO: Implement pagination
        response.setPageSize(flightPaths.size());

        return response;
    }

    @Cacheable(value = "searchCache", key = "#p0.sourceAirportId + ':' + #p0.destinationAirportId + ':' + #p0.date")
    public FlightDetails getFlightPaths(SearchRequest request) {
        Airport source = getAirport(request.getSourceAirportId());
        Airport destination = getAirport(request.getDestinationAirportId());

        LocalDateTime searchDate = request.getDate().atStartOfDay();

        List<List<Flight>> paths = pathFindingService.findPaths(
                source, destination, searchDate, getMaxStops(request.getFilters())
        );

        FlightDetails flightDetails = new FlightDetails();
        flightDetails.setFlightPath(paths);
        return  flightDetails;
    }
    
    @Cacheable(value = "flightCache", key = "#p0", unless = "#result == null")
    public FlightDetailsResponse getFlightDetails(String flightNumber) {
        List<Flight> flights = flightRepository.findByFlightNumber(flightNumber);

        if (flights.isEmpty()) {
            throw new RuntimeException("Flight not found with number: " + flightNumber);
        }

        // Get the first flight (you might want to add logic to handle multiple flights)
        Flight flight = flights.get(0);
        FlightDetailsResponse response = convertToFlightDetails(flight);

        // Enrich with latest price and seat data
        BigDecimal cachedPrice = cacheService.getCachedPrice(flight.getId());
        if (cachedPrice != null) {
            response.setPrice(cachedPrice);
        }

        Integer cachedSeats = cacheService.getCachedSeats(flight.getId());
        if (cachedSeats != null) {
            response.setAvailableSeats(cachedSeats);
        }

        return response;
    }
    
    @Transactional
    @CacheEvict(value = "priceCache", key = "#p0.flightId")
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
    @CacheEvict(value = "seatCache", key = "#p0.flightId")
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
    
    private FlightPath convertToFlightPath(List<Flight> flights) {
        FlightPath path = new FlightPath();
        path.setFlights(flights.stream()
            .map(this::convertToFlightInfo)
            .collect(Collectors.toList()));

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Flight flight : flights) {
            BigDecimal cachedPrice = cacheService.getCachedPrice(flight.getId());
            totalPrice = totalPrice.add(cachedPrice != null ? cachedPrice : flight.getPrice());
        }
        path.setTotalPrice(totalPrice);

        path.setTotalStops(flights.size() - 1);

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

        BigDecimal cachedPrice = cacheService.getCachedPrice(flight.getId());
        info.setPrice(cachedPrice != null ? cachedPrice : flight.getPrice());
        
        info.setDepartureTime(flight.getDepartureTime());
        info.setArrivalTime(flight.getArrivalTime());

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

        AirportInfo sourceAirport = new AirportInfo();
        sourceAirport.setId(flight.getSourceAirport().getId());
        sourceAirport.setName(flight.getSourceAirport().getName());
        sourceAirport.setCode(flight.getSourceAirport().getCode());
        response.setSourceAirport(sourceAirport);

        AirportInfo destAirport = new AirportInfo();
        destAirport.setId(flight.getDestinationAirport().getId());
        destAirport.setName(flight.getDestinationAirport().getName());
        destAirport.setCode(flight.getDestinationAirport().getCode());
        response.setDestinationAirport(destAirport);
        
        return response;
    }
    
    private int getMaxStops(SearchFilters filters) {
        if (filters == null || filters.getStops() == null) {
            return 2;
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
    
    private Airport getAirport(Long id) {
        return airportRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Airport not found with id: " + id));
    }
} 