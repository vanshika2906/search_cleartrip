package org.example.service;

import org.example.model.Flight;
import org.example.model.Airport;
import org.example.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PathFindingService {
    
    @Autowired
    private FlightRepository flightRepository;
    
    public List<List<Flight>> findPaths(Airport source, Airport destination, LocalDateTime date, int maxStops) {
        List<List<Flight>> allPaths = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        List<Flight> currentPath = new ArrayList<>();
        
        findPathsDFS(source, destination, date, maxStops, visited, currentPath, allPaths);
        return allPaths;
    }
    
    private void findPathsDFS(Airport current, Airport destination, LocalDateTime date, 
                            int remainingStops, Set<String> visited, 
                            List<Flight> currentPath, List<List<Flight>> allPaths) {
        if (current.equals(destination)) {
            allPaths.add(new ArrayList<>(currentPath));
            return;
        }
        
        if (remainingStops < 0) {
            return;
        }
        
        // Get all possible next flights from current airport
        List<Flight> nextFlights = getNextFlights(current, date);
        
        for (Flight flight : nextFlights) {
            String flightKey = flight.getId();
            if (!visited.contains(flightKey)) {
                // Check if this flight's departure time is after the last flight's arrival time
                // and within the same day as the search date
                if (isValidConnection(currentPath, flight) && 
                    flight.getDepartureTime().toLocalDate().equals(date.toLocalDate())) {
                    visited.add(flightKey);
                    currentPath.add(flight);
                    
                    findPathsDFS(flight.getDestinationAirport(), destination, 
                               flight.getArrivalTime(), remainingStops - 1, 
                               visited, currentPath, allPaths);
                    
                    currentPath.remove(currentPath.size() - 1);
                    visited.remove(flightKey);
                }
            }
        }
    }
    
    private boolean isValidConnection(List<Flight> currentPath, Flight nextFlight) {
        if (currentPath.isEmpty()) {
            return true;
        }
        
        Flight lastFlight = currentPath.get(currentPath.size() - 1);
        // Ensure there's enough time between flights (e.g., 30 minutes)
        return nextFlight.getDepartureTime().isAfter(
            lastFlight.getArrivalTime().plusMinutes(30)
        );
    }
    
    private List<Flight> getNextFlights(Airport airport, LocalDateTime afterTime) {
        return flightRepository.findNextFlights(airport, afterTime);
    }
} 