package com.kaicode.rmi.util;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Helper class for handling text encoding issues across different platforms.
 * <p>
 * This utility class provides comprehensive solutions for UTF-8 encoding problems
 * commonly encountered in Java applications, particularly on Windows environments.
 * It addresses mojibake (corrupted character encoding) issues, console encoding setup,
 * and cross-platform text processing requirements.
 * <p>
 * Key responsibilities:
 * <ul>
 *   <li>Automated correction of common UTF-8 mojibake patterns</li>
 *   <li>Windows-specific console encoding configuration</li>
 *   <li>Cross-platform text encoding detection and setup</li>
 *   <li>Console output stream reconfiguration for proper Unicode support</li>
 *   <li>Integration with logging frameworks (Logback, java.util.logging)</li>
 * </ul>
 * <p>
 * Usage is designed to be transparent and fail-safe, with best-effort approaches
 * that won't break applications if encoding fixes are unsuccessful.
 * <p>
 * Recommended initialization (call as first statement in main method):
 * <pre>{@code
 * EncodingHelper.setupUTF8ConsoleStreams();
 * // Now console output supports full Unicode, including box-drawing chars
 * }</pre>
 *
 * @since 1.0
 * @see EncodingInitializer
 * @see UTF8Console
 */
public class EncodingHelper {

    /**
     * Creates a UTF-8 enabled PrintWriter for console output.
     * <p>
     * Wraps System.out with UTF-8 encoding to ensure proper display
     * of Unicode characters including emojis and special symbols.
     * The PrintWriter is configured for auto-flushing.
     *
     * @return PrintWriter configured with UTF-8 encoding, never null
     */
    public static PrintWriter createUTF8PrintWriter() {
        return new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true);
    }

    /**
     * Cleans text for Windows console output by fixing mojibake (corrupted UTF-8 sequences).
     * <p>
     * This method detects and repairs common UTF-8 encoding errors that occur when UTF-8 text
     * is incorrectly interpreted as Windows-1252 or ISO-8859-1. This encoding corruption
     * commonly happens in GitBash, Windows CMD, and other console environments.
     * <p>
     * Repaired character mappings include:
     * <ul>
     *   <li>"ΓòÉ" → "═" (Box Drawings Double Horizontal)</li>
     *   <li>"ΓöÇ" → "─" (Box Drawings Light Horizontal)</li>
     *   <li>"Γû¬" → "▪" (Black Small Square)</li>
     *   <li>Smart quote mojibake → standard quotes</li>
     *   <li>Dash variants → standard hyphens</li>
     * </ul>
     * <p>
     * The method also removes control characters while preserving newlines,
     * tabs, and carriage returns for proper text formatting.
     *
     * @param text the text to clean, may be null
     * @return cleaned text with mojibake repaired and control chars removed,
     *         empty string if input was null
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
     * <p>
     * Platform detection is performed by examining the "os.name" system property
     * and checking for case-insensitive substring presence of "win".
     * This covers various Windows variants including "Windows XP", "Windows 10", etc.
     *
     * @return true if running on Windows platform, false otherwise
     */
    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }

    /**
     * Sets up UTF-8 encoding for Windows console by changing code page.
     * <p>
     * Executes "chcp 65001" command to set Windows console to UTF-8 code page (65001).
     * This is critical for displaying Unicode characters correctly in Windows CMD.
     * The method performs best-effort execution and silently fails if the command
     * cannot be executed (not fatal for application operation).
     * <p>
     * Should be called before {@link #setupUTF8ConsoleStreams()} for optimal Windows support.
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
     * Sets UTF-8 as the default console encoding with comprehensive platform support.
     * <p>
     * Performs multi-step UTF-8 configuration to ensure proper Unicode display:
     * <ol>
     *   <li>Set system properties to prefer UTF-8 encoding</li>
     *   <li>Configure Windows console code page (if applicable)</li>
     *   <li>Flush existing output buffers</li>
     *   <li>Reconfigure System.out and System.err with UTF-8 wrappers</li>
     *   <li>Reinitialize logging frameworks (Logback, java.util.logging)</li>
     * </ol>
     * <p>
     * This is a CRITICAL method for proper Unicode display in Windows/GitBash.
     * Should be called as early as possible, ideally as the very first statement
     * in the application's main() method.
     * <p>
     * Designed with fail-safe approach - silent degradation to default encoding
     * if UTF-8 configuration is not possible.
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
     * Sets UTF-8 as the default console encoding (legacy method).
     *
     * @deprecated Use {@link #setupUTF8ConsoleStreams()} instead for enhanced functionality
     */
    @Deprecated
    public static void configureConsoleEncoding() {
        setupUTF8ConsoleStreams();
    }

    /**
     * Removes emoji characters from text for environments that don't support them.
     * <p>
     * Filters out Unicode emoji and other symbol characters defined in Unicode ranges
     * So (Other Symbol) and Cn (Unassigned). Useful for console output in environments
     * where emoji display might cause issues.
     * <p>
     * Note: This is a conservative approach that may remove some valid symbols
     * beyond just emojis (e.g., mathematical symbols).
     *
     * @param text the text containing potential emojis
     * @return text with emoji and symbol characters removed, empty string if input null
     */
    public static String removeEmojis(String text) {
        if (text == null) {
            return "";
        }

        // Remove emojis by filtering out characters in emoji ranges
        return text.replaceAll("[\\p{So}\\p{Cn}]", "");
    }

    /**
     * Checks if the console supports UTF-8 encoding based on current configuration.
     * <p>
     * Performs heuristic detection by examining the "file.encoding" system property.
     * Returns true if the property contains "utf" (case-insensitive).
     * This is not a definitive test but provides reasonable confidence.
     *
     * @return true if UTF-8 encoding is likely supported, false otherwise
     */
    public static boolean isUTF8Supported() {
        String encoding = System.getProperty("file.encoding");
        return encoding != null && encoding.toLowerCase().contains("utf");
    }
}
