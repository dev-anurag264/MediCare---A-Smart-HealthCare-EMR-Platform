package com.medicare_health_systems.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenInvalidateService {

    private final RedisTemplate<String, String> redisTemplate;

    private static String INVALIDATE_PREFIX = "blacklist";

    public void blacklistToken(String token, long expiryMillis) {
        String key = INVALIDATE_PREFIX + token;
        redisTemplate.opsForValue().set(key, "true", expiryMillis, TimeUnit.MILLISECONDS);
        log.info("Token blacklisted, expires in {}ms", expiryMillis);
    }

    public boolean isTokenBlacklisted(String token) {
        String key = INVALIDATE_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}

