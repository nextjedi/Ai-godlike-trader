package com.nextjedi.trading.tipbasedtrading.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextjedi.trading.tipbasedtrading.broker.dto.Tick;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Produces tick data to Redis Stream for real-time processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TickDataProducer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${trading.tick-data.redis.stream-key:trading:ticks}")
    private String streamKey;

    @Value("${trading.tick-data.enabled:true}")
    private boolean tickDataEnabled;

    /**
     * Publish a tick to Redis Stream
     */
    public void publishTick(Tick tick) {
        if (!tickDataEnabled) {
            return;
        }

        try {
            // Create stream entry
            Map<String, Object> tickData = new HashMap<>();
            tickData.put("instrumentToken", tick.getInstrumentToken().toString());
            tickData.put("tradingSymbol", tick.getTradingSymbol());
            tickData.put("lastPrice", tick.getLastPrice());
            tickData.put("volume", tick.getVolume());
            tickData.put("bidPrice", tick.getBidPrice());
            tickData.put("askPrice", tick.getAskPrice());
            tickData.put("openInterest", tick.getOpenInterest());
            tickData.put("timestamp", tick.getTimestamp().toString());

            // Publish to stream
            redisTemplate.opsForStream().add(streamKey, tickData);

            log.trace("Published tick for {} to Redis Stream", tick.getTradingSymbol());

            // Also cache latest tick with TTL for quick access
            String cacheKey = "tick:latest:" + tick.getInstrumentToken();
            redisTemplate.opsForValue().set(cacheKey, tick,
                    java.time.Duration.ofSeconds(86400)); // 24 hours TTL

        } catch (Exception e) {
            log.error("Failed to publish tick to Redis: {}", e.getMessage(), e);
        }
    }

    /**
     * Get latest tick from cache
     */
    public Tick getLatestTick(Long instrumentToken) {
        try {
            String cacheKey = "tick:latest:" + instrumentToken;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.convertValue(cached, Tick.class);
            }
        } catch (Exception e) {
            log.error("Failed to get latest tick from cache: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Publish multiple ticks in batch
     */
    public void publishTicksBatch(java.util.List<Tick> ticks) {
        ticks.forEach(this::publishTick);
    }
}
