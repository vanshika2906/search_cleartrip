package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final ConcurrentHashMap<String, AtomicInteger> cacheMissCounters = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_MISSES = 3;
    private static final long TEMP_NULL_TTL_SECONDS = 60;

    // ==================== PRICE CACHE ====================

    public BigDecimal getCachedPrice(String flightId) {
        String stampedeKey = "stampede:price:" + flightId;

        if (shouldPreventCacheStampede(stampedeKey)) {
            return null;
        }

        return getPriceFromCache(flightId);
    }

    @Cacheable(value = "priceCache", key = "#p0", unless = "#result == null")
    public BigDecimal getPriceFromCache(String flightId) {
        return null;
    }

    @CacheEvict(value = "priceCache", key = "#p0")
    public void invalidatePriceCache(String flightId) {
        // Spring will auto-delete priceCache::flightId
    }

    // ==================== SEAT CACHE ====================

    public Integer getCachedSeats(String flightId) {
        String stampedeKey = "stampede:seat:" + flightId;

        if (shouldPreventCacheStampede(stampedeKey)) {
            return null;
        }

        return getSeatsFromCache(flightId);
    }

    @Cacheable(value = "seatCache", key = "#p0", unless = "#result == null")
    public Integer getSeatsFromCache(String flightId) {
        return null;
    }

    @CacheEvict(value = "seatCache", key = "#p0")
    public void invalidateSeatCache(String flightId) {
        // Spring handles this
    }

    // ==================== SEARCH CACHE ====================

    @CacheEvict(value = "searchCache", key = "#p0")
    public void invalidateSearchCache(String searchKey) {
        // Use same cache key pattern as in @Cacheable in SearchService
    }

    // ==================== STAMPEDE PROTECTION ====================
    private boolean shouldPreventCacheStampede(String stampedeKey) {
        AtomicInteger missCounter = cacheMissCounters.computeIfAbsent(stampedeKey, k -> new AtomicInteger(0));
        int misses = missCounter.incrementAndGet();

        if (misses >= MAX_CACHE_MISSES) {
            missCounter.set(0);

            // Optional Redis TTL-based lock
            redisTemplate.opsForValue().set(stampedeKey, "LOCKED", TEMP_NULL_TTL_SECONDS, TimeUnit.SECONDS);
            return true;
        }

        return false;
    }
}
