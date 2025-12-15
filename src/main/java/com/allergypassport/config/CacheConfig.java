package com.allergypassport.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Configuration for application-level caching.
 * Enables caching for allergen-related data to improve performance.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure cache manager with predefined caches for allergen data.
     * Uses in-memory concurrent map caches for simplicity.
     *
     * Cache names:
     * - "allergens": All allergens list (ordered by category and display order)
     * - "allergensByCategory": Allergens grouped by category
     * - "allergen": Individual allergen lookup by code
     * - "categories": All allergen categories
     *
     * @return Configured cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("allergens"),
                new ConcurrentMapCache("allergensByCategory"),
                new ConcurrentMapCache("allergen"),
                new ConcurrentMapCache("categories")
        ));
        return cacheManager;
    }
}
