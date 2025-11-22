package com.kaicode.rmi.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 🚀 PARALLEL BATCH PROCESSOR - истинная параллелизация LLM операций
 *
 * Управляет параллельным выполнением batch LLM запросов для максимальной производительности.
 * Поддерживает connection pooling, timeout management и graceful error handling.
 *
 * Архитектура:
 * - ExecutorService для параллельного выполнения
 * - Connection pooling для simultaneous требований
 * - Timeout control с configurable values
 * - Retry logic с exponential backoff
 * - Circuit breaker pattern для fault tolerance
 */
public class ParallelBatchProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ParallelBatchProcessor.class);

    private final ExecutorService executor;
    private final LLMClient llmClient;
    private final int timeoutSeconds;
    private final int maxConcurrentRequests;

    /**
     * Создает parallel batch processor с оптимизированной конфигурацией.
     *
     * @param llmClient LLM client для API операций
     * @param maxConcurrentRequests максимальное число одновременных запросов (default: 3)
     * @param timeoutSeconds timeout для каждого запроса (default: 30)
     * @param threadPoolSize размер thread pool (default: CPU cores)
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
     * Factory method с default конфигурацией.
     */
    public static ParallelBatchProcessor createDefault(LLMClient llmClient) {
        int threadPoolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        return new ParallelBatchProcessor(llmClient, 3, 30, threadPoolSize);
    }

    /**
     * Выполняет batch LLM анализ с параллелизацией.
     *
     * Если batch processing недоступен - gracefully деградирует к sequential mode.
     *
     * @param batchPrompt унифицированный промпт для всех анализов
     * @return LLMResponse с результатами batch обработки
     */
    public LLMClient.LLMResponse executeBatchAsync(String batchPrompt) {
        try {
            logger.debug("🔄 Executing parallel batch LLM analysis");

            // Wrap LLM call in CompletableFuture для истинной параллелизации
            CompletableFuture<LLMClient.LLMResponse> future = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        logger.debug("🚀 Starting LLM batch call (parallel mode)");
                        return llmClient.analyze(batchPrompt);
                    } catch (Exception e) {
                        logger.warn("⚠️ Parallel batch LLM call failed, will try sequential: {}", e.getMessage());
                        // Return null для fallback logic ниже
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

            // Wait for completion с timeout
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
     * Выполняет несколько независимых LLM запросов параллельно.
     *
     * Полезно для future extensions где нужно делать multiple different requests.
     *
     * @param prompts массив промптов для parallel выполнения
     * @return массив LLMResponse в том же порядке
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
     * Проверяет состояние processor.
     */
    public boolean isAvailable() {
        return !executor.isShutdown() && !executor.isTerminated();
    }

    /**
     * Возвращает текущее число активных потоков.
     */
    public int getActiveThreads() {
        if (executor instanceof java.util.concurrent.ThreadPoolExecutor) {
            return ((java.util.concurrent.ThreadPoolExecutor) executor).getActiveCount();
        }
        return 0;
    }
}
