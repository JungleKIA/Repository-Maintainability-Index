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
     * - "â€œ" should be """ (left double quote)
     * - "â€"" should be "–" (en dash)
     * 
     * @param text the text to clean
     * @return cleaned text with mojibake repaired
     */
    public static String cleanTextForWindows(String text) {
        if (text == null) {
            return "";
        }

        String cleaned = text;
        
        // Replace common mojibake patterns from box-drawing characters
        // These occur when UTF-8 bytes are interpreted as Windows-1252/Latin-1
        cleaned = cleaned.replace("ΓòÉ", "═");  // Box Drawings Double Horizontal (U+2550)
        cleaned = cleaned.replace("ΓöÇ", "─");  // Box Drawings Light Horizontal (U+2500)
        cleaned = cleaned.replace("Γû¬", "▪");  // Black Small Square (U+25AA)
        cleaned = cleaned.replace("Γöé", "│");  // Box Drawings Light Vertical (U+2502)
        cleaned = cleaned.replace("ΓöÇΓöÇΓöÇ", "───");  // Multiple dashes
        
        // Replace common mojibake patterns from punctuation
        // Note: Using escape sequences to avoid compilation issues with UTF-8 characters in source
        cleaned = cleaned.replace("\u00E2\u0080\u009C", "\""); // Left double quote mojibake
        cleaned = cleaned.replace("\u00E2\u0080\u009D", "\""); // Right double quote mojibake
        cleaned = cleaned.replace("\u00E2\u0080\u0098", "'");  // Left single quote mojibake
        cleaned = cleaned.replace("\u00E2\u0080\u0099", "'");  // Right single quote mojibake
        cleaned = cleaned.replace("\u00E2\u0080\u0093", "-");  // En dash mojibake
        cleaned = cleaned.replace("\u00E2\u0080\u0094", "-");  // Em dash mojibake
        cleaned = cleaned.replace("\u00E2\u0080\u00A6", "..."); // Ellipsis mojibake
        
        // Additional dash variants from real-world Git Bash issues
        cleaned = cleaned.replace("ΓÇæ", "-");  // Dash variant (common in LLM responses)
        cleaned = cleaned.replace("ΓÇô", "-");  // Dash variant (common in LLM responses)
        
        // Additional common patterns
        cleaned = cleaned.replace("\u00C2", "");     // Stray non-breaking space marker
        cleaned = cleaned.replace("\u00C3\u00A9", "e");  // e with acute
        cleaned = cleaned.replace("\u00C3\u00A8", "e");  // e with grave
        cleaned = cleaned.replace("\u00C3\u00A0", "a");  // a with grave
        
        // Remove control characters except newlines and tabs
        cleaned = cleaned.replaceAll("[\\p{Cntrl}&&[^\n\t\r]]", "");
        
        return cleaned.trim();
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
                
                // Additional Windows-specific properties
                System.setProperty("sun.stdout.encoding", "UTF-8");
                System.setProperty("sun.stderr.encoding", "UTF-8");
            }
            
            // Step 3: Flush existing buffers before reconfiguration
            System.out.flush();
            System.err.flush();
            
            // Step 4: Wrap existing System.out and System.err with UTF-8 PrintStreams
            // CRITICAL: Wrap existing streams, don't replace them
            // This approach is compatible with Java 10+ and works in GitBash
            System.setOut(new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new java.io.PrintStream(System.err, true, StandardCharsets.UTF_8));
            
            // Step 5: Reinitialize Logback to use new System.out/err streams
            // This is CRITICAL because Logback captures System.out during initialization
            try {
                Class<?> loggerFactoryClass = Class.forName("org.slf4j.LoggerFactory");
                Object iLoggerFactory = loggerFactoryClass.getMethod("getILoggerFactory").invoke(null);
                
                Class<?> loggerContextClass = Class.forName("ch.qos.logback.classic.LoggerContext");
                if (loggerContextClass.isInstance(iLoggerFactory)) {
                    Object loggerContext = iLoggerFactory;
                    loggerContextClass.getMethod("reset").invoke(loggerContext);
                    
                    Class<?> contextInitializerClass = Class.forName("ch.qos.logback.classic.util.ContextInitializer");
                    Object contextInitializer = contextInitializerClass.getConstructor(loggerContextClass).newInstance(loggerContext);
                    contextInitializerClass.getMethod("autoConfig").invoke(contextInitializer);
                }
            } catch (Exception e) {
                // Ignore - Logback might not be present or configuration might fail
            }
            
            // Step 6: Configure java.util.logging to use UTF-8
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
