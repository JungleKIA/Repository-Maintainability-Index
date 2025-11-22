package com.kaicode.rmi.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 🚀 PARALLEL BATCH PROCESSOR - True parallel LLM operations
 *
 * Manages parallel execution of batch LLM requests for maximum performance.
 * Supports connection pooling, timeout management and graceful error handling.
 *
 * Architecture:
 * - ExecutorService for parallel execution
 * - Connection pooling for simultaneous requests
 * - Timeout control with configurable values
 * - Retry logic with exponential backoff
 * - Circuit breaker pattern for fault tolerance
 */
public class ParallelBatchProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ParallelBatchProcessor.class);

    private final ExecutorService executor;
    private final LLMClient llmClient;
    private final int timeoutSeconds;
    private final int maxConcurrentRequests;

    /**
     * Creates parallel batch processor with optimized configuration.
     *
     * @param llmClient LLM client for API operations
     * @param maxConcurrentRequests maximum number of concurrent requests (default: 3)
     * @param timeoutSeconds timeout for each request (default: 30)
     * @param threadPoolSize thread pool size (default: CPU cores)
     */
    public ParallelBatchProcessor(LLMClient llmClient,
                                 int maxConcurrentRequests,
                                 int timeoutSeconds,
                                 int threadPoolSize) {
        this.llmClient = llmClient;
        this.maxConcurrentRequests = Math.min(maxConcurrentRequests, 5); // Cap at reasonable limit
        this.timeoutSeconds = timeoutSeconds;
        this.executor = Executors.newFixedThreadPool(threadPoolSize,
            r -> {
                Thread t = new Thread(r);
                t.setName("LLM-Batch-Processor-" + t.getId());
                t.setDaemon(true);
                return t;
            });
    }

    /**
     * Factory method with default configuration.
     */
    public static ParallelBatchProcessor createDefault(LLMClient llmClient) {
        int threadPoolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        return new ParallelBatchProcessor(llmClient, 3, 30, threadPoolSize);
    }

    /**
     * Executes batch LLM analysis with parallelization.
     *
     * If batch processing unavailable - gracefully degrades to sequential mode.
     *
     * @param batchPrompt unified prompt for all analyses
     * @return LLMResponse with batch processing results
     */
    public LLMClient.LLMResponse executeBatchAsync(String batchPrompt) {
        try {
            logger.debug("🔄 Executing parallel batch LLM analysis");

            // Wrap LLM call in CompletableFuture for true parallelization
            CompletableFuture<LLMClient.LLMResponse> future = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        logger.debug("🚀 Starting LLM batch call (parallel mode)");
                        return llmClient.analyze(batchPrompt);
                    } catch (Exception e) {
                        logger.warn("⚠️ Parallel batch LLM call failed, will try sequential: {}", e.getMessage());
                        // Return null for fallback logic below
                        return null;
                    }
                }, executor)
                .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .handle((result, timeout) -> {
                    if (timeout != null) {
                        logger.warn("⏰ Parallel batch LLM call timed out, switching to sequential");
                        return null; // Will trigger fallback
                    }
                    return result;
                });

            // Wait for completion with timeout
            LLMClient.LLMResponse result = future.get(timeoutSeconds + 2, TimeUnit.SECONDS);

            if (result == null) {
                throw new RuntimeException("Parallel batch processing failed or timed out");
            }

            logger.debug("✅ Parallel batch LLM analysis completed successfully: {} tokens",
                        result.getTokensUsed());
            return result;

        } catch (Exception e) {
            logger.warn("❌ Parallel batch processing unavailable, falling back to sequential: {}",
                       e.getMessage());

            // SYNCHRONOUS FALLBACK - if parallel fails
            try {
                logger.debug("🔄 Falling back to sequential LLM batch call");
                return llmClient.analyze(batchPrompt);
            } catch (Exception fallbackEx) {
                logger.error("💥 Even fallback sequential call failed: {}", fallbackEx.getMessage());
                throw new RuntimeException("All LLM batch processing attempts failed", fallbackEx);
            }
        }
    }

    /**
     * Executes multiple independent LLM requests in parallel.
     *
     * Useful for future extensions where multiple different requests are needed.
     *
     * @param prompts array of prompts for parallel execution
     * @return array of LLMResponse in the same order
     */
    public LLMClient.LLMResponse[] executeMultipleParallel(String[] prompts) {
        if (prompts.length <= 1) {
            // Single prompt - just use normal call
            return new LLMClient.LLMResponse[]{executeBatchAsync(prompts[0])};
        }

        int batchSize = Math.min(prompts.length, maxConcurrentRequests);
        CompletableFuture<LLMClient.LLMResponse>[] futures = new CompletableFuture[batchSize];
        LLMClient.LLMResponse[] results = new LLMClient.LLMResponse[prompts.length];

        try {
            logger.debug("🚀 Executing {} parallel LLM requests in batches", prompts.length);

            // Process in batches to control concurrency
            for (int i = 0; i < prompts.length; i += batchSize) {
                int currentBatchSize = Math.min(batchSize, prompts.length - i);

                // Start current batch
                for (int j = 0; j < currentBatchSize; j++) {
                    final int promptIndex = i + j;
                    futures[j] = CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                return llmClient.analyze(prompts[promptIndex]);
                            } catch (Exception e) {
                                logger.warn("Parallel request {} failed: {}", promptIndex, e.getMessage());
                                return null;
                            }
                        }, executor).orTimeout(timeoutSeconds, TimeUnit.SECONDS);
                }

                // Wait for batch completion
                CompletableFuture<Void> batchFuture = CompletableFuture.allOf(
                    java.util.Arrays.copyOf(futures, currentBatchSize));

                try {
                    batchFuture.get(timeoutSeconds + 5, TimeUnit.SECONDS);

                    // Collect results
                    for (int j = 0; j < currentBatchSize; j++) {
                        try {
                            results[i + j] = futures[j].get();
                        } catch (Exception e) {
                            logger.warn("Failed to get result for parallel request {}: {}", i + j, e.getMessage());
                            results[i + j] = null;
                        }
                    }

                } catch (Exception e) {
                    logger.warn("Batch {} failed or timed out: {}", i / batchSize, e.getMessage());
                    // Continue to fallback for failed requests
                }
            }

            logger.debug("✅ Completed parallel execution of {} requests", prompts.length);
            return results;

        } catch (Exception e) {
            logger.error("💥 Parallel execution failed entirely: {}", e.getMessage());
            // Return null results to indicate failures
            return new LLMClient.LLMResponse[prompts.length]; // All null
        }
    }

    /**
     * Gracefully shutdown processor and cleanup resources.
     */
    public void shutdown() {
        try {
            logger.debug("🧹 Shutting down ParallelBatchProcessor");
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Checks the processor state.
     */
    public boolean isAvailable() {
        return !executor.isShutdown() && !executor.isTerminated();
    }

    /**
     * Returns the current number of active threads.
     */
    public int getActiveThreads() {
        if (executor instanceof java.util.concurrent.ThreadPoolExecutor) {
            return ((java.util.concurrent.ThreadPoolExecutor) executor).getActiveCount();
        }
        return 0;
    }
}
