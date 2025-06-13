package org.example.service;

import org.example.dto.FlightDetails;
import org.example.dto.SearchRequest;
import org.example.model.Airport;
import org.example.model.Flight;
import org.example.repository.AirportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FlightPathService {

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private PathFindingService pathFindingService;

    @Cacheable(value = "searchCache", key = "#p0.sourceAirportId + ':' + #p0.destinationAirportId + ':' + #p0.date", unless = "#result == null")
    public FlightDetails getFlightPaths(SearchRequest request) {
        Airport source = airportRepository.findById(request.getSourceAirportId())
                .orElseThrow(() -> new RuntimeException("Source airport not found"));

        Airport destination = airportRepository.findById(request.getDestinationAirportId())
                .orElseThrow(() -> new RuntimeException("Destination airport not found"));

        LocalDateTime searchDate = request.getDate().atStartOfDay();

        List<List<Flight>> paths = pathFindingService.findPaths(
                source, destination, searchDate, 2  // or call getMaxStops if needed
        );

        FlightDetails details = new FlightDetails();
        details.setFlightPath(paths);
        return details;
    }
}
