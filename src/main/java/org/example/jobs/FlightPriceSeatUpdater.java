package org.example.jobs;

import org.example.dto.PriceUpdateRequest;
import org.example.dto.SeatUpdateRequest;
import org.example.model.Flight;
import org.example.repository.FlightRepository;
import org.example.service.CacheService;
import org.example.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Component
public class FlightPriceSeatUpdater {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private SearchService searchService;

    private final Random random = new Random();

    // 🕒 Every 5 minutes
    @Scheduled(cron = "0 */5 * * * *")
    public void simulatePriceAndSeatFluctuations() {
        List<Flight> upcomingFlights = flightRepository.findByFlightDateAfter(LocalDate.now().minusDays(1));

        for (Flight flight : upcomingFlights) {
            boolean changed = false;

            // Simulate price change
            BigDecimal currentPrice = flight.getPrice();
            BigDecimal fluctuation = currentPrice.multiply(BigDecimal.valueOf(random.nextDouble(0.05, 0.15)));
            BigDecimal newPrice = random.nextBoolean()
                    ? currentPrice.add(fluctuation)
                    : currentPrice.subtract(fluctuation);
            newPrice = newPrice.max(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP); // min price floor
            if (!newPrice.equals(currentPrice)) {
                PriceUpdateRequest priceUpdate = new PriceUpdateRequest();
                priceUpdate.setFlightId(flight.getId());
                priceUpdate.setNewPrice(newPrice);
                searchService.updatePrice(priceUpdate);
                changed = true;
            }

            // Simulate seat availability change
            int seatDelta = random.nextInt(5) + 1;
            int newAvailableSeats = random.nextBoolean()
                    ? flight.getAvailableSeats() + seatDelta
                    : flight.getAvailableSeats() - seatDelta;
            newAvailableSeats = Math.min(newAvailableSeats, flight.getTotalSeats());
            newAvailableSeats = Math.max(0, newAvailableSeats);

            if (newAvailableSeats != flight.getAvailableSeats()) {
                SeatUpdateRequest seatUpdate = new SeatUpdateRequest();
                seatUpdate.setFlightId(flight.getId());
                seatUpdate.setAvailableSeats(newAvailableSeats);
                searchService.updateSeats(seatUpdate);
                changed = true;
            }

            if (changed) {
                System.out.println("Updated flight: " + flight.getFlightNumber()
                        + " | Price: " + newPrice
                        + " | Seats: " + newAvailableSeats);
            }
        }
    }
}
