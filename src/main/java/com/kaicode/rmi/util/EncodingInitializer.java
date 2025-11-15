package com.kaicode.rmi.util;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * CRITICAL: This class MUST be loaded BEFORE any logging framework initialization.
 * It sets up UTF-8 encoding for console streams to ensure proper Unicode display
 * without requiring -Dfile.encoding=UTF-8 JVM flag.
 * 
 * This approach is based on the working solution from repository-maintainability-index project.
 */
public class EncodingInitializer {
    
    private static boolean initialized = false;
    
    static {
        // This static block runs when the class is loaded
        // It MUST run before Logback/SLF4J initialization
        initializeEncoding();
    }
    
    /**
     * Initialize UTF-8 encoding for console streams.
     * This method wraps System.out and System.err with UTF-8 PrintStreams,
     * making Unicode display work regardless of JVM file.encoding setting.
     */
    private static void initializeEncoding() {
        if (initialized) {
            return;
        }
        
        try {
            // Step 1: For Windows, set console code page to UTF-8
            if (isWindows()) {
                try {
                    ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "chcp 65001 > nul");
                    pb.inheritIO();
                    Process process = pb.start();
                    process.waitFor();
                } catch (Exception e) {
                    // Ignore - not critical
                }
            }
            
            // Step 2: Flush existing streams
            System.out.flush();
            System.err.flush();
            
            // Step 3: Wrap System.out and System.err with UTF-8 PrintStreams
            // This is the KEY to making Unicode work without -Dfile.encoding=UTF-8
            PrintStream utf8Out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
            PrintStream utf8Err = new PrintStream(System.err, true, StandardCharsets.UTF_8);
            
            System.setOut(utf8Out);
            System.setErr(utf8Err);
            
            initialized = true;
        } catch (Exception e) {
            // Silently fail - application will continue with default encoding
        }
    }
    
    /**
     * Force initialization of encoding.
     * Call this method as early as possible in your application.
     */
    public static void ensureInitialized() {
        // Just accessing this class triggers the static initializer
        // This method exists to provide an explicit way to trigger initialization
    }
    
    private static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }
}
