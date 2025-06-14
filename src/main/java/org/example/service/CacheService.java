package org.example.service;

import org.example.model.Flight;
import org.example.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private FlightRepository flightRepository;

    private final ConcurrentHashMap<String, AtomicInteger> cacheMissCounters = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_MISSES = 3;
    private static final long TEMP_NULL_TTL_SECONDS = 60;
    private static final long CACHE_TTL_HOURS = 1;

    // ============ PRICE CACHE ============

    public BigDecimal getCachedPrice(String flightId) {
        String cacheKey = "price:" + flightId;
        String stampedeKey = "stampede:price:" + flightId;

        if (shouldPreventCacheStampede(stampedeKey)) {
            logger.warn("Cache stampede prevention triggered for price: {}", flightId);
            return null;
        }

        try {
            String cachedValue = redisTemplate.opsForValue().get(cacheKey);
            if (cachedValue != null) {
                resetCacheMissCounter(stampedeKey);
                return new BigDecimal(cachedValue);
            }
        } catch (Exception e) {
            logger.error("Error reading price from cache for flight: {}", flightId, e);
        }

        return getPriceFromCache(flightId);
    }

    @Cacheable(value = "priceCache", key = "#p0", unless = "#result == null")
    public BigDecimal getPriceFromCache(String flightId) {
        return flightRepository.findById(flightId)
                .map(flight -> {
                    try {
                        String cacheKey = "price:" + flightId;
                        redisTemplate.opsForValue().set(cacheKey, flight.getPrice().toString(), CACHE_TTL_HOURS, TimeUnit.HOURS);
                        return flight.getPrice();
                    } catch (Exception e) {
                        logger.error("Error caching price for flight: {}", flightId, e);
                        return null;
                    }
                })
                .orElse(null);
    }

    public void setCachedPrice(String flightId, BigDecimal price) {
        if (price == null) {
            logger.warn("Attempted to cache null price for flight: {}", flightId);
            return;
        }

        try {
            String cacheKey = "price:" + flightId;
            redisTemplate.opsForValue().set(cacheKey, price.toString(), CACHE_TTL_HOURS, TimeUnit.HOURS);
            logger.info("Price cache updated for flight: {}", flightId);
        } catch (Exception e) {
            logger.error("Error setting price cache for flight: {}", flightId, e);
        }
    }

    @CacheEvict(value = "priceCache", key = "#p0")
    public void invalidatePriceCache(String flightId) {
        try {
            String cacheKey = "price:" + flightId;
            redisTemplate.delete(cacheKey);
            logger.info("Price cache invalidated for flight: {}", flightId);
        } catch (Exception e) {
            logger.error("Error invalidating price cache for flight: {}", flightId, e);
        }
    }

    // ============ SEAT CACHE ============

    public Integer getCachedSeats(String flightId) {
        String cacheKey = "seat:" + flightId;
        String stampedeKey = "stampede:seat:" + flightId;

        if (shouldPreventCacheStampede(stampedeKey)) {
            logger.warn("Cache stampede prevention triggered for seats: {}", flightId);
            return null;
        }

        try {
            String cachedValue = redisTemplate.opsForValue().get(cacheKey);
            if (cachedValue != null) {
                resetCacheMissCounter(stampedeKey);
                return Integer.parseInt(cachedValue);
            }
        } catch (Exception e) {
            logger.error("Error reading seats from cache for flight: {}", flightId, e);
        }

        return getSeatsFromCache(flightId);
    }

    @Cacheable(value = "seatCache", key = "#p0", unless = "#result == null")
    public Integer getSeatsFromCache(String flightId) {
        return flightRepository.findById(flightId)
                .map(flight -> {
                    try {
                        String cacheKey = "seat:" + flightId;
                        redisTemplate.opsForValue().set(cacheKey, flight.getAvailableSeats().toString(), CACHE_TTL_HOURS, TimeUnit.HOURS);
                        return flight.getAvailableSeats();
                    } catch (Exception e) {
                        logger.error("Error caching seats for flight: {}", flightId, e);
                        return null;
                    }
                })
                .orElse(null);
    }

    public void setCachedSeats(String flightId, Integer seats) {
        if (seats == null) {
            logger.warn("Attempted to cache null seats for flight: {}", flightId);
            return;
        }

        try {
            String cacheKey = "seat:" + flightId;
            redisTemplate.opsForValue().set(cacheKey, seats.toString(), CACHE_TTL_HOURS, TimeUnit.HOURS);
            logger.info("Seats cache updated for flight: {}", flightId);
        } catch (Exception e) {
            logger.error("Error setting seats cache for flight: {}", flightId, e);
        }
    }

    @CacheEvict(value = "seatCache", key = "#p0")
    public void invalidateSeatCache(String flightId) {
        try {
            String cacheKey = "seat:" + flightId;
            redisTemplate.delete(cacheKey);
            logger.info("Seats cache invalidated for flight: {}", flightId);
        } catch (Exception e) {
            logger.error("Error invalidating seats cache for flight: {}", flightId, e);
        }
    }

    // ============ SEARCH CACHE ============

    @CacheEvict(value = "searchCache", key = "#p0")
    public void invalidateSearchCache(String searchKey) {
        try {
            String cacheKey = "search:" + searchKey;
            redisTemplate.delete(cacheKey);
            logger.info("Search cache invalidated for key: {}", searchKey);
        } catch (Exception e) {
            logger.error("Error invalidating search cache for key: {}", searchKey, e);
        }
    }

    // ============ CACHE MAINTENANCE ============

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void refreshActiveCaches() {
        logger.info("Starting cache refresh job");
        try {
            List<Flight> activeFlights = flightRepository.findAllFlights();
            for (Flight flight : activeFlights) {
                setCachedPrice(flight.getId(), flight.getPrice());
                setCachedSeats(flight.getId(), flight.getAvailableSeats());
            }
            logger.info("Cache refresh completed for {} flights", activeFlights.size());
        } catch (Exception e) {
            logger.error("Error during cache refresh", e);
        }
    }

    // ============ STAMPEDE PREVENTION ============

    private boolean shouldPreventCacheStampede(String stampedeKey) {
        AtomicInteger missCounter = cacheMissCounters.computeIfAbsent(stampedeKey, k -> new AtomicInteger(0));
        int misses = missCounter.incrementAndGet();

        if (misses >= MAX_CACHE_MISSES) {
            missCounter.set(0);
            try {
                redisTemplate.opsForValue().set(stampedeKey, "LOCKED", TEMP_NULL_TTL_SECONDS, TimeUnit.SECONDS);
                logger.warn("Cache stampede prevention activated for key: {}", stampedeKey);
            } catch (Exception e) {
                logger.error("Error setting stampede lock for key: {}", stampedeKey, e);
            }
            return true;
        }

        return false;
    }

    private void resetCacheMissCounter(String stampedeKey) {
        cacheMissCounters.remove(stampedeKey);
    }
}
