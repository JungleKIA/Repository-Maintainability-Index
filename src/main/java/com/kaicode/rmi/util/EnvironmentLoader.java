package com.kaicode.rmi.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for loading environment variables from .env file
 * Automatically loads .env file if it exists, with system environment variables taking precedence
 * Uses lazy initialization to avoid loading during static initialization
 */
public class EnvironmentLoader {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentLoader.class);
    private static volatile Dotenv dotenv;
    private static volatile boolean initialized = false;

    /**
     * Lazy initialization of .env file loading
     * This ensures UTF-8 encoding is set up before any logging occurs
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
     * Get environment variable value, checking system environment first, then .env file
     * @param key Environment variable key
     * @return Value of the environment variable or null if not found
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
     * Get environment variable value with default fallback
     * @param key Environment variable key
     * @param defaultValue Default value if variable is not found
     * @return Value of the environment variable or default value
     */
    public static String getEnv(String key, String defaultValue) {
        String value = getEnv(key);
        return value != null ? value : defaultValue;
    }
}