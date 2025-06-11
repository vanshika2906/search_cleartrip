package org.example.service;

import org.example.dto.FlightDetailsResponse;
import org.example.dto.FlightPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // Track cache misses to prevent cache stampede
    private final ConcurrentHashMap<String, AtomicInteger> cacheMissCounters = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_MISSES = 3;

    // Search Cache Operations
    //@Cacheable(value = "searchCache", key = "#sourceAirportId + ':' + #destinationAirportId + ':' + #date")
    public List<FlightPath> getCachedPaths(Long sourceAirportId, Long destinationAirportId, String date) {
        return null; // Will be populated by the service layer
    }

    //@CacheEvict(value = "searchCache", key = "#searchKey")
    public void invalidateSearchCache(String searchKey) {
        // Method to explicitly invalidate specific search cache
    }

    // Price Cache Operations
    //@Cacheable(value = "priceCache", key = "#flightId", unless = "#result == null")
    public BigDecimal getCachedPrice(String flightId) {
        String cacheKey = "price:" + flightId;
        if (shouldPreventCacheStampede(cacheKey)) {
            return null;
        }
        return null; // Will be populated by the service layer
    }

    //@CacheEvict(value = "priceCache", key = "#flightId")
    public void invalidatePriceCache(String flightId) {
        // Method to explicitly invalidate price cache for a specific flight
    }

    public void setCachedPrice(String flightId, BigDecimal price) {
        if (price == null) {
            return;
        }
        String key = "price:" + flightId;
        redisTemplate.opsForValue().set(key, price.toString(), 1, TimeUnit.HOURS);
    }

    // Seat Cache Operations
    //@Cacheable(value = "seatCache", key = "#flightId", unless = "#result == null")
    public Integer getCachedSeats(String flightId) {
        String cacheKey = "seat:" + flightId;
        if (shouldPreventCacheStampede(cacheKey)) {
            return null;
        }
        return null; // Will be populated by the service layer
    }

    //@CacheEvict(value = "seatCache", key = "#flightId")
    public void invalidateSeatCache(String flightId) {
        // Method to explicitly invalidate seat cache for a specific flight
    }

    public void setCachedSeats(String flightId, Integer seats) {
        if (seats == null) {
            return;
        }
        String key = "seats:" + flightId;
        redisTemplate.opsForValue().set(key, seats.toString(), 1, TimeUnit.HOURS);
    }

    // Flight Details Cache Operations
    //@Cacheable(value = "flightCache", key = "#flightId", unless = "#result == null")
    public FlightDetailsResponse getCachedFlightDetails(String flightId) {
        String cacheKey = "flight:" + flightId;
        if (shouldPreventCacheStampede(cacheKey)) {
            return null;
        }
        return null; // Will be populated by the service layer
    }

    //@CacheEvict(value = "flightCache", key = "#flightId")
    public void invalidateFlightCache(String flightId) {
        // Method to explicitly invalidate flight details cache
    }

    // Helper method to prevent cache stampede
    private boolean shouldPreventCacheStampede(String cacheKey) {
        AtomicInteger missCounter = cacheMissCounters.computeIfAbsent(cacheKey, k -> new AtomicInteger(0));
        int misses = missCounter.incrementAndGet();
        
        if (misses >= MAX_CACHE_MISSES) {
            missCounter.set(0); // Reset counter after max misses
            return true;
        }
        return false;
    }

    // Reset cache miss counter when data is successfully cached
    public void resetCacheMissCounter(String cacheKey) {
        cacheMissCounters.remove(cacheKey);
    }
} 