package org.example.repository;

import org.example.model.Flight;
import org.example.model.Airport;
import org.example.model.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, String> {
    
    @Query("SELECT f FROM Flight f WHERE f.sourceAirport = :airport " +
           "AND f.departureTime >= :afterTime " +
           "ORDER BY f.departureTime")
    List<Flight> findNextFlights(@Param("airport") Airport airport, 
                                @Param("afterTime") LocalDateTime afterTime);
    
    @Query("SELECT f FROM Flight f WHERE f.sourceAirport.id = :sourceId " +
           "AND f.destinationAirport.id = :destId " +
           "AND f.flightDate = :date")
    List<Flight> findDirectFlights(@Param("sourceId") Long sourceId,
                                  @Param("destId") Long destId,
                                  @Param("date") LocalDateTime date);

    @Query("SELECT f FROM Flight f WHERE f.sourceAirport.id = :sourceId " +
           "AND f.destinationAirport.id = :destId " +
           "AND f.departureTime BETWEEN :startDate AND :endDate")
    List<Flight> findBySourceAndDestinationAndDateRange(
        @Param("sourceId") Long sourceId,
        @Param("destId") Long destId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT f FROM Flight f WHERE f.departureTime BETWEEN :startDate AND :endDate")
    List<Flight> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT f FROM Flight f WHERE f.departureTime BETWEEN :startDate AND :endDate " +
           "AND (:sourceId IS NULL OR f.sourceAirport.id = :sourceId) " +
           "AND (:destId IS NULL OR f.destinationAirport.id = :destId)")
    List<Flight> findByDateRangeAndAirports(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("sourceId") Long sourceId,
        @Param("destId") Long destId
    );

    @Query("SELECT f FROM Flight f WHERE f.departureTime >= :afterTime " +
           "AND f.sourceAirport.id = :airportId " +
           "ORDER BY f.departureTime ASC")
    List<Flight> findNextFlights(
        @Param("airportId") Long airportId,
        @Param("afterTime") LocalDateTime afterTime
    );

    @Query("SELECT f FROM Flight f WHERE f.flightNumber = :flightNumber " +
           "AND f.departureTime BETWEEN :startDate AND :endDate")
    List<Flight> findByFlightNumberAndDateRange(
        @Param("flightNumber") String flightNumber,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT f FROM Flight f WHERE f.status = :status " +
           "AND f.departureTime BETWEEN :startDate AND :endDate")
    List<Flight> findByStatusAndDateRange(
        @Param("status") FlightStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
} 