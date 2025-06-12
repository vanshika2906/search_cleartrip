package org.example.dto;

import lombok.Data;
import org.example.model.Flight;

import java.util.List;

@Data
public class FlightDetails {

    public List<List<Flight>> flightPath;

}
