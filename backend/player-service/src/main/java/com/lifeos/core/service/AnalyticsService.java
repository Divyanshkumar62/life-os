package com.lifeos.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service("redisAnalyticsService")
@RequiredArgsConstructor
public class AnalyticsService {

    private final StringRedisTemplate redisTemplate;

    private static final String HEATMAP_KEY_PREFIX = "player:analytics:heatmap:";
    private static final long CACHE_TTL_DAYS = 7;

    /**
     * Write-Through cache update. Updates the heatmap status for a player and a date in Redis.
     */
    public void updateHeatmap(UUID playerId, LocalDate date, String status) {
        String key = HEATMAP_KEY_PREFIX + playerId;
        redisTemplate.opsForHash().put(key, date.toString(), status);
    }

    /**
     * Fetch the heatmap. Relies entirely on Redis, falling back to a database mock repopulation on cache miss.
     */
    public Map<String, String> getHeatmap(UUID playerId) {
        String key = HEATMAP_KEY_PREFIX + playerId;
        
        // Fetch all fields from the hash
        Map<Object, Object> rawEntries = redisTemplate.opsForHash().entries(key);
        Map<String, String> heatmap = new HashMap<>();

        if (rawEntries == null || rawEntries.isEmpty()) {
            // Cache Miss: Simulate DB fetch and repopulate
            heatmap = fetchAndCacheFromDb(playerId);
        } else {
            for (Map.Entry<Object, Object> entry : rawEntries.entrySet()) {
                heatmap.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }

        return heatmap;
    }

    private Map<String, String> fetchAndCacheFromDb(UUID playerId) {
        String key = HEATMAP_KEY_PREFIX + playerId;
        Map<String, String> dbData = new HashMap<>();

        // Generate mock historical quest statuses for last 5 days as a database fallback
        LocalDate today = LocalDate.now();
        dbData.put(today.minusDays(4).toString(), "ALL_CLEARED");
        dbData.put(today.minusDays(3).toString(), "PARTIAL_CLEARED");
        dbData.put(today.minusDays(2).toString(), "STEALTH_PAUSED");
        dbData.put(today.minusDays(1).toString(), "FAILED");
        dbData.put(today.toString(), "ALL_CLEARED");

        // Repopulate cache
        redisTemplate.opsForHash().putAll(key, dbData);

        return dbData;
    }
}
