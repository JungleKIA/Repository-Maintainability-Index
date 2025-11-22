package com.kaicode.rmi.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Smart cache manager for LLM results.
 * <p>
 * Improves performance and reduces costs through intelligent caching.
 * Features:
 * <ul>
 *   <li>Content-based hashing for unique identification</li>
 *   <li>TTL-based expiration for data freshness</li>
 *   <li>LRU eviction for memory optimization</li>
 *   <li>Repository-specific cache keys</li>
 *   <li>Hit/miss ratio statistics</li>
 * </ul>
 *
 * @since 1.0
 */
public class LLMCacheManager {
    private static final Logger logger = LoggerFactory.getLogger(LLMCacheManager.class);

    // Cache structures
    private final Map<String, CacheEntry> cache;
    private final Map<String, LinkedList<String>> lruList; // LRU tracking per repository
    private final Set<String> repositories;

    // Configuration
    private final long ttlMillis;
    private final int maxEntriesPerRepository;
    private final int maxTotalEntries;

    // Statistics
    private long hits = 0;
    private long misses = 0;

    /**
     * Cache entry with metadata for management.
     *
     * @since 1.0
     */
    private static class CacheEntry {
        final LLMClient.LLMResponse response;
        final long expireTimeMillis;
        final long createdTimeMillis;

        CacheEntry(LLMClient.LLMResponse response, long expireTimeMillis) {
            this.response = response;
            this.expireTimeMillis = expireTimeMillis;
            this.createdTimeMillis = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTimeMillis;
        }

        boolean isValid() {
            return !isExpired() && response != null && response.getContent() != null;
        }
    }

    /**
     * Creates a smart cache manager with optimized configuration.
     *
     * @param ttlHours cache time-to-live in hours (default: 24 hours)
     * @param maxEntriesPerRepository maximum entries per repository (default: 50)
     * @param maxTotalEntries maximum total entries across all repositories (default: 1000)
     * @since 1.0
     */
    public LLMCacheManager(int ttlHours, int maxEntriesPerRepository, int maxTotalEntries) {
        this.ttlMillis = ttlHours * 60 * 60 * 1000L; // Convert to milliseconds
        this.maxEntriesPerRepository = maxEntriesPerRepository;
        this.maxTotalEntries = maxTotalEntries;

        this.cache = new ConcurrentHashMap<>();
        this.lruList = new ConcurrentHashMap<>();
        this.repositories = ConcurrentHashMap.newKeySet();

        logger.debug("🚀 Initialized LLMCacheManager: TTL={}h, maxPerRepo={}, maxTotal={}",
                    ttlHours, maxEntriesPerRepository, maxTotalEntries);
    }

    /**
     * Factory method with default configuration.
     * <p>
     * Creates a cache manager with 24 hours TTL, 50 entries per repository,
     * and 1000 total entries.
     *
     * @return a new cache manager with default settings
     * @since 1.0
     */
    public static LLMCacheManager createDefault() {
        return new LLMCacheManager(24, 50, 1000); // 24 hours, 50 per repo, 1000 total
    }

    /**
     * Attempts to retrieve a cached result for repository analysis.
     *
     * @param owner repository owner
     * @param repo repository name
     * @param contentHash hash of input content (README + commits)
     * @return cached LLMResponse, or null if not in cache or expired
     * @since 1.0
     */
    public LLMClient.LLMResponse get(String owner, String repo, String contentHash) {
        String key = generateCacheKey(owner, repo, contentHash);

        CacheEntry entry = cache.get(key);
        if (entry == null || !entry.isValid()) {
            misses++;
            cleanupExpired();
            logger.debug("💨 Cache miss for {}/{}: {}", owner, repo, contentHash.substring(0, 16) + "...");
            return null;
        }

        // Update LRU order
        updateLRU(owner, repo, key);

        hits++;
        logger.debug("✅ Cache hit for {}/{}: {} (age: {} ms)",
                    owner, repo, contentHash.substring(0, 16) + "...",
                    System.currentTimeMillis() - entry.createdTimeMillis);
        return entry.response;
    }

    /**
     * Stores a result in the cache.
     *
     * @param owner repository owner
     * @param repo repository name
     * @param contentHash hash of input content
     * @param response LLM response to cache
     * @since 1.0
     */
    public void put(String owner, String repo, String contentHash, LLMClient.LLMResponse response) {
        if (response == null || response.getContent() == null) {
            logger.debug("⚠️ Not caching null response for {}/{}", owner, repo);
            return;
        }

        String key = generateCacheKey(owner, repo, contentHash);
        long expireTime = System.currentTimeMillis() + ttlMillis;

        CacheEntry entry = new CacheEntry(response, expireTime);
        cache.put(key, entry);

        // Track repository for LRU management
        repositories.add(generateRepoKey(owner, repo));
        updateLRU(owner, repo, key);

        // Cleanup if needed
        cleanupIfNeeded(owner, repo);

        logger.debug("📦 Cached response for {}/{}: {} chars, TTL={} hours",
                    owner, repo, response.getContent().length(), ttlMillis / (1000 * 60 * 60));
    }

    /**
     * Checks if a result is contained in the cache.
     *
     * @param owner repository owner
     * @param repo repository name
     * @param contentHash hash of input content
     * @return true if cached and valid, false otherwise
     * @since 1.0
     */
    public boolean contains(String owner, String repo, String contentHash) {
        String key = generateCacheKey(owner, repo, contentHash);
        CacheEntry entry = cache.get(key);
        return entry != null && entry.isValid();
    }

    /**
     * Clears the cache for a specific repository.
     *
     * @param owner repository owner
     * @param repo repository name
     * @since 1.0
     */
    public void clearRepository(String owner, String repo) {
        String repoKey = generateRepoKey(owner, repo);
        LinkedList<String> repoLRU = lruList.remove(repoKey);

        if (repoLRU != null) {
            repoLRU.forEach(cache::remove);
        }

        repositories.remove(repoKey);
        logger.debug("🧹 Cleared cache for repository {}/{} ({} entries removed)",
                    owner, repo, repoLRU != null ? repoLRU.size() : 0);
    }

    /**
     * Clears the entire cache.
     *
     * @since 1.0
     */
    public void clearAll() {
        cache.clear();
        lruList.clear();
        repositories.clear();
        resetStats();
        logger.debug("🧹 Cleared entire cache");
    }

    /**
     * Returns cache statistics.
     *
     * @return statistics about cache performance and usage
     * @since 1.0
     */
    public CacheStats getStats() {
        long totalRequests = hits + misses;
        double hitRate = totalRequests > 0 ? (hits * 100.0) / totalRequests : 0.0;

        return new CacheStats(
            cache.size(),
            repositories.size(),
            hits,
            misses,
            hitRate,
            ttlMillis,
            maxEntriesPerRepository,
            maxTotalEntries
        );
    }

    /**
     * Resets hit/miss statistics.
     *
     * @since 1.0
     */
    public void resetStats() {
        hits = 0;
        misses = 0;
    }

    /**
     * Performs cache maintenance by cleaning up expired entries and enforcing limits.
     *
     * @since 1.0
     */
    public void maintenance() {
        cleanupExpired();
        enforceLimits();
    }

    // Private helper methods

    private String generateCacheKey(String owner, String repo, String contentHash) {
        return String.format("%s/%s:%s", owner, repo, contentHash);
    }

    private String generateRepoKey(String owner, String repo) {
        return owner + "/" + repo;
    }

    private void updateLRU(String owner, String repo, String key) {
        String repoKey = generateRepoKey(owner, repo);
        LinkedList<String> repoLRU = lruList.computeIfAbsent(repoKey, k -> new LinkedList<>());

        // Remove from current position if exists, then add to front
        repoLRU.remove(key);
        repoLRU.addFirst(key);
    }

    private void cleanupIfNeeded(String owner, String repo) {
        String repoKey = generateRepoKey(owner, repo);
        LinkedList<String> repoLRU = lruList.get(repoKey);

        if (repoLRU != null && repoLRU.size() > maxEntriesPerRepository) {
            // Remove least recently used for this repository
            String lruKey = repoLRU.removeLast();
            cache.remove(lruKey);
            logger.debug("🗑️ LRU eviction for {}/{}: removed 1 entry", owner, repo);
        }

        if (cache.size() > maxTotalEntries) {
            enforceTotalLimit();
        }
    }

    private void enforceLimits() {
        cleanupExpired();

        if (cache.size() > maxTotalEntries) {
            enforceTotalLimit();
        }

        // Per-repository limits are already enforced in cleanupIfNeeded
        for (String repoKey : repositories) {
            LinkedList<String> repoLRU = lruList.get(repoKey);
            if (repoLRU != null && repoLRU.size() > maxEntriesPerRepository) {
                while (repoLRU.size() > maxEntriesPerRepository) {
                    String lruKey = repoLRU.removeLast();
                    cache.remove(lruKey);
                }
                logger.debug("🗑️ Per-repository LRU: reduced {} to {}", repoKey, maxEntriesPerRepository);
            }
        }
    }

    private void enforceTotalLimit() {
        // Global LRU eviction across all repositories
        List<Map.Entry<String, LinkedList<String>>> reposBySize = lruList.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
            .toList();

        int toRemove = cache.size() - maxTotalEntries;
        int removed = 0;

        for (Map.Entry<String, LinkedList<String>> repo : reposBySize) {
            if (removed >= toRemove) break;

            LinkedList<String> repoLRU = repo.getValue();
            while (!repoLRU.isEmpty() && removed < toRemove) {
                String lruKey = repoLRU.removeLast();
                cache.remove(lruKey);
                removed++;
            }
        }

        logger.debug("🗑️ Global LRU: removed {} entries to stay within {} limit", removed, maxTotalEntries);
    }

    private void cleanupExpired() {
        List<String> expiredKeys = cache.entrySet().stream()
            .filter(entry -> entry.getValue().isExpired())
            .map(Map.Entry::getKey)
            .toList();

        for (String key : expiredKeys) {
            cache.remove(key);

            // Remove from LRU tracking
            for (LinkedList<String> repoLRU : lruList.values()) {
                repoLRU.remove(key);
            }
        }

        if (!expiredKeys.isEmpty()) {
            logger.debug("🧹 Cleaned up {} expired cache entries", expiredKeys.size());
        }
    }

    /**
     * Generates a content hash for caching.
     * <p>
     * Uses SHA-256 for consistency. Falls back to simple hash if SHA-256 is unavailable.
     *
     * @param content the content to hash
     * @return a hash string (first 16 characters of Base64-encoded SHA-256)
     * @since 1.0
     */
    public static String generateContentHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash).substring(0, 16); // First 16 chars for brevity
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash if SHA-256 unavailable
            return String.valueOf(content.hashCode()).replace("-", "");
        }
    }

    /**
     * Statistics container for cache analytics.
     *
     * @since 1.0
     */
    public static class CacheStats {
        public final int totalEntries;
        public final int totalRepositories;
        public final long hits;
        public final long misses;
        public final double hitRate;
        public final long ttlMillis;
        public final int maxPerRepository;
        public final int maxTotal;

        CacheStats(int totalEntries, int totalRepositories, long hits, long misses,
                  double hitRate, long ttlMillis, int maxPerRepository, int maxTotal) {
            this.totalEntries = totalEntries;
            this.totalRepositories = totalRepositories;
            this.hits = hits;
            this.misses = misses;
            this.hitRate = hitRate;
            this.ttlMillis = ttlMillis;
            this.maxPerRepository = maxPerRepository;
            this.maxTotal = maxTotal;
        }

        @Override
        public String toString() {
            return String.format(
                "CacheStats{entries=%d, repos=%d, hits=%d, misses=%d, hitRate=%.1f%%, ttl=%dh, maxPerRepo=%d, maxTotal=%d}",
                totalEntries, totalRepositories, hits, misses, hitRate,
                ttlMillis / (60 * 60 * 1000), maxPerRepository, maxTotal
            );
        }
    }
}
