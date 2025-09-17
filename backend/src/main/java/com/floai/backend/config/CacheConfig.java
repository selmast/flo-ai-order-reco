package com.floai.backend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enable Spring caching and define a Caffeine cache for recommendations.
 * Matches the @Cacheable("recoByOrder") you already have in RecommendationEngine.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Cache name(s) used in @Cacheable/@CacheEvict
        CaffeineCacheManager mgr = new CaffeineCacheManager("recoByOrder");
        // ~10 minutes TTL, cap entries to avoid runaway memory
        mgr.setCacheSpecification("maximumSize=500,expireAfterWrite=10m");
        return mgr;
    }
}
