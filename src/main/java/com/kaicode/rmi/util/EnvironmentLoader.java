package com.kaicode.rmi.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe environment variable loader with .env file support.
 * <p>
 * Provides unified access to environment variables from both system environment
 * and optional .env file, implementing a hierarchical lookup strategy with lazy initialization.
 * System environment variables take precedence over .env file entries.
 * <p>
 * Key design principles:
 * <ul>
 *   <li>Lazy initialization to avoid loading during static initialization</li>
 *   <li>Thread-safe singleton pattern for .env file loading</li>
 *   <li>Fault-tolerant design with graceful .env file absence handling</li>
 *   <li>No early logging to prevent encoding issues before UTF-8 setup</li>
 *   <li>Immutable string return values for thread safety</li>
 * </ul>
 * <p>
 * Loading precedence (highest to lowest):
 * <ol>
 *   <li>System environment variables (set by OS/JVM)</li>
 *   <li>.env file entries (optional local configuration)</li>
 *   <li>Default values (provided by caller)</li>
 * </ol>
 * <p>
 * File location: The .env file is expected in the current working directory.
 * If not found, operation silently continues with system environment only.
 * <p>
 * Typical usage for API keys and sensitive configuration:
 * <pre>{@code
 * // .env file content:
 * // GITHUB_TOKEN=ghp_1234567890abcdef
 * // OPENAI_API_KEY=sk-1234567890abcdef
 *
 * public class ApiClient {
 *     private static final String GITHUB_TOKEN = EnvironmentLoader.getEnv("GITHUB_TOKEN");
 *     private static final String OPENAI_KEY = EnvironmentLoader.getEnv("OPENAI_API_KEY");
 *
 *     public void initialize() {
 *         if (GITHUB_TOKEN == null) {
 *             throw new IllegalStateException("GITHUB_TOKEN environment variable required");
 *         }
 *         // Configuration loaded safely
 *     }
 * }
 * }</pre>
 *
 * @since 1.0
 */
public class EnvironmentLoader {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentLoader.class);
    private static volatile Dotenv dotenv;
    private static volatile boolean initialized = false;

    /**
     * Lazy initialization of .env file loading with thread-safe singleton pattern.
     * <p>
     * Ensures .env file is loaded exactly once, even in multi-threaded environments.
     * Uses double-checked locking pattern for optimal performance.
     * Initialization occurs silently to avoid encoding issues during early startup.
     * <p>
     * Configuration: File is optional (ignoreIfMissing), loaded from working directory.
     * Any exceptions during loading are silently ignored to prevent application failures.
     * <p>
     * Thread safety: All access protected by class-level synchronization.
     */
    private static void initialize() {
        if (!initialized) {
            synchronized (EnvironmentLoader.class) {
                if (!initialized) {
                    try {
                        dotenv = Dotenv.configure()
                                .ignoreIfMissing()
                                .load();
                        // Note: Logging removed to avoid encoding issues during early initialization
                        // The .env file is loaded silently
                    } catch (Exception e) {
                        // Silently fail - .env file is optional
                        // Logging here could cause encoding issues if called before UTF-8 setup
                    }
                    initialized = true;
                }
            }
        }
    }

    /**
     * Retrieves environment variable value with hierarchical lookup.
     * <p>
     * Searches for the specified key in order of precedence:
     * system environment first, then .env file. Returns null if not found in either location.
     * <p>
     * Thread safety: Method is thread-safe and can be called concurrently from multiple threads.
     * Lazy initialization ensures .env loading occurs only when first needed.
     *
     * @param key the environment variable name, must not be null
     * @return the variable value, or null if not found in system or .env file
     */
    public static String getEnv(String key) {
        // Check system environment first (no initialization needed)
        String value = System.getenv(key);
        if (value != null) {
            return value;
        }

        // Initialize .env file if needed
        initialize();

        if (dotenv != null) {
            return dotenv.get(key);
        }
        return null;
    }

    /**
     * Retrieves environment variable value with default fallback.
     * <p>
     * Searches for the variable using same precedence as {@link #getEnv(String)},
     * returning the provided default value if the key is not found in either location.
     * <p>
     * This method is particularly useful for optional configuration with sensible defaults.
     *
     * @param key the environment variable name, must not be null
     * @param defaultValue default value to return if variable is not found, may be null
     * @return the variable value from environment/.env, or defaultValue if not found
     */
    public static String getEnv(String key, String defaultValue) {
        String value = getEnv(key);
        return value != null ? value : defaultValue;
    }
}
