package com.kaicode.rmi.util;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Helper class for handling text encoding issues across different platforms.
 * 
 * This class provides utilities to ensure proper UTF-8 encoding in console output,
 * particularly for Windows environments where the default console encoding may not
 * support Unicode characters like emojis.
 */
public class EncodingHelper {

    /**
     * Creates a UTF-8 enabled PrintWriter for console output.
     * 
     * @return PrintWriter configured with UTF-8 encoding
     */
    public static PrintWriter createUTF8PrintWriter() {
        return new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true);
    }

    /**
     * Cleans text for Windows console output by fixing mojibake (corrupted UTF-8 sequences).
     * 
     * This method detects and repairs common UTF-8 encoding errors that occur when UTF-8 text
     * is incorrectly interpreted as Windows-1252 or ISO-8859-1, which is common in GitBash
     * and Windows console environments.
     * 
     * Common mojibake patterns for box-drawing characters:
     * - "ΓòÉ" should be "═" (U+2550)
     * - "ΓöÇ" should be "─" (U+2500)
     * - "Γû¬" should be "▪" (U+25AA)
     * 
     * @param text the text to clean
     * @return cleaned text with mojibake repaired
     */
    public static String cleanTextForWindows(String text) {
        if (text == null) {
            return "";
        }

        // Check if we're running on Windows
        if (!isWindows()) {
            return text;
        }

        // Detect if text contains mojibake patterns (corrupted UTF-8)
        // If it does, try to repair it by re-encoding
        if (containsMojibake(text)) {
            try {
                // Try to fix mojibake by re-encoding:
                // 1. Get bytes as if text was incorrectly decoded as ISO-8859-1
                byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);
                // 2. Re-decode as UTF-8 (the original intended encoding)
                return new String(bytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                // If repair fails, return original text
                return text;
            }
        }

        // No mojibake detected, return as-is
        return text;
    }

    /**
     * Detects if text contains mojibake (corrupted UTF-8 sequences).
     * 
     * This method checks for common mojibake patterns that occur when UTF-8 characters
     * are incorrectly interpreted as Windows-1252 or ISO-8859-1.
     * 
     * @param text the text to check
     * @return true if mojibake patterns are detected
     */
    private static boolean containsMojibake(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // Check for common mojibake patterns
        // These are UTF-8 box-drawing characters incorrectly decoded as Windows-1252/ISO-8859-1
        return text.contains("ΓòÉ") ||  // corrupted ═ (U+2550)
               text.contains("ΓöÇ") ||  // corrupted ─ (U+2500)
               text.contains("Γû¬") ||  // corrupted ▪ (U+25AA)
               text.contains("Γöé") ||  // corrupted │ (U+2502)
               text.contains("ΓöÇΓöÇ") || // multiple corrupted ─
               text.contains("ΓòÉΓòÉ");  // multiple corrupted ═
    }

    /**
     * Checks if the current platform is Windows.
     * 
     * @return true if running on Windows, false otherwise
     */
    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }

    /**
     * Sets up UTF-8 encoding for Windows console.
     * 
     * This method specifically handles Windows console code page setup.
     * It should be called before setupUTF8ConsoleStreams().
     */
    public static void setupUTF8Output() {
        if (!isWindows()) {
            return;
        }

        try {
            // Execute chcp 65001 to set console to UTF-8
            // This is critical for Windows CMD and sometimes helps with GitBash
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "chcp", "65001");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Consume output to prevent blocking
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                while (reader.readLine() != null) {
                    // Consume and discard output
                }
            }
            
            process.waitFor();
        } catch (Exception e) {
            // Silently fail - not critical if this doesn't work
        }
    }

    /**
     * Sets UTF-8 as the default console encoding (best effort).
     * 
     * This method attempts to configure the console to use UTF-8 encoding.
     * This is critical for Windows/GitBash to display Unicode box-drawing characters correctly.
     * 
     * Call this method as early as possible in your application's main() method,
     * ideally as the very first statement.
     */
    public static void setupUTF8ConsoleStreams() {
        try {
            // Step 1: Set system properties to prefer UTF-8
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("sun.jnu.encoding", "UTF-8");
            System.setProperty("console.encoding", "UTF-8");
            
            // Step 2: For Windows, set console code page to UTF-8 (65001)
            if (isWindows()) {
                setupUTF8Output();
            }
            
            // Step 3: Reconfigure System.out and System.err with UTF-8 encoding
            // Use PrintStream with explicit UTF-8 charset for compatibility with GitBash
            // Important: charset parameter is only available in Java 10+
            try {
                // Try Java 10+ constructor with Charset parameter
                System.setOut(new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8));
                System.setErr(new java.io.PrintStream(System.err, true, StandardCharsets.UTF_8));
            } catch (Exception e) {
                // Fallback for older Java or if above fails
                // Wrap the output streams with UTF-8 OutputStreamWriter
                System.setOut(new java.io.PrintStream(
                    new java.io.BufferedOutputStream(
                        new java.io.FileOutputStream(java.io.FileDescriptor.out), 
                        128
                    ),
                    true,  // autoFlush
                    StandardCharsets.UTF_8.name()
                ));
                
                System.setErr(new java.io.PrintStream(
                    new java.io.BufferedOutputStream(
                        new java.io.FileOutputStream(java.io.FileDescriptor.err),
                        128
                    ),
                    true,  // autoFlush
                    StandardCharsets.UTF_8.name()
                ));
            }
            
            // Step 4: Configure java.util.logging to use UTF-8
            try {
                java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
                java.util.logging.Handler[] handlers = rootLogger.getHandlers();
                for (java.util.logging.Handler handler : handlers) {
                    if (handler instanceof java.util.logging.ConsoleHandler) {
                        handler.setEncoding("UTF-8");
                    }
                }
            } catch (Exception e) {
                // Ignore - logging configuration is optional
            }
        } catch (Exception e) {
            // Silently fail - we'll use default encoding
            // This is a best-effort attempt to fix encoding issues
        }
    }

    /**
     * Sets UTF-8 as the default console encoding (best effort).
     * 
     * @deprecated Use {@link #setupUTF8ConsoleStreams()} instead
     */
    @Deprecated
    public static void configureConsoleEncoding() {
        setupUTF8ConsoleStreams();
    }

    /**
     * Removes emoji characters from text for environments that don't support them.
     * 
     * @param text the text containing emojis
     * @return text with emojis removed
     */
    public static String removeEmojis(String text) {
        if (text == null) {
            return "";
        }

        // Remove emojis by filtering out characters in emoji ranges
        return text.replaceAll("[\\p{So}\\p{Cn}]", "");
    }

    /**
     * Checks if the console supports UTF-8 encoding.
     * 
     * @return true if UTF-8 is likely supported, false otherwise
     */
    public static boolean isUTF8Supported() {
        String encoding = System.getProperty("file.encoding");
        return encoding != null && encoding.toLowerCase().contains("utf");
    }
}
