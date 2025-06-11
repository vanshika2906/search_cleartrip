package org.example.dto.catalogue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class FlightEntryRequest {
    private Long sourceAirportId;
    private Long destinationAirportId;
    private LocalTime departureTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> daysAllowed;
    private String flightNumber;
    private Integer totalSeats;
    private BigDecimal price;

    // Getters and Setters
    public Long getSourceAirportId() {
        return sourceAirportId;
    }

    public void setSourceAirportId(Long sourceAirportId) {
        this.sourceAirportId = sourceAirportId;
    }

    public Long getDestinationAirportId() {
        return destinationAirportId;
    }

    public void setDestinationAirportId(Long destinationAirportId) {
        this.destinationAirportId = destinationAirportId;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<String> getDaysAllowed() {
        return daysAllowed;
    }

    public void setDaysAllowed(List<String> daysAllowed) {
        this.daysAllowed = daysAllowed;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }


} 