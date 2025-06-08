package org.example.dto.catalogue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class FlightScheduleResponse {
    private String flightEntryId;
    private String flightNumber;
    private LocalTime departureTime;
    private List<String> daysAllowed;
    private List<LocalDate> cancelledDates;
    private Integer totalSeats;

    // Getters and Setters
    public String getFlightEntryId() {
        return flightEntryId;
    }

    public void setFlightEntryId(String flightEntryId) {
        this.flightEntryId = flightEntryId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public List<String> getDaysAllowed() {
        return daysAllowed;
    }

    public void setDaysAllowed(List<String> daysAllowed) {
        this.daysAllowed = daysAllowed;
    }

    public List<LocalDate> getCancelledDates() {
        return cancelledDates;
    }

    public void setCancelledDates(List<LocalDate> cancelledDates) {
        this.cancelledDates = cancelledDates;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }
} 