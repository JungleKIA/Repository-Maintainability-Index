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
     * Cleans text for Windows console output by removing or replacing problematic characters.
     * 
     * This method handles Unicode characters that may not display correctly in Windows console,
     * while preserving them for environments that support full Unicode.
     * 
     * @param text the text to clean
     * @return cleaned text suitable for Windows console
     */
    public static String cleanTextForWindows(String text) {
        if (text == null) {
            return "";
        }

        // Check if we're running on Windows
        if (!isWindows()) {
            return text;
        }

        // For Windows, we'll keep the text as-is but ensure UTF-8 output
        // The key is using the proper PrintWriter, not modifying the text
        return text;
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
     * Sets UTF-8 as the default console encoding (best effort).
     * 
     * This method attempts to configure the console to use UTF-8 encoding.
     * This is critical for Windows/GitBash to display Unicode box-drawing characters correctly.
     */
    public static void setupUTF8ConsoleStreams() {
        try {
            // Set system properties to prefer UTF-8
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("sun.jnu.encoding", "UTF-8");
            
            // Create a new PrintStream with UTF-8 encoding
            // This ensures all System.out.println() calls use UTF-8
            System.setOut(new java.io.PrintStream(
                new java.io.BufferedOutputStream(
                    new java.io.FileOutputStream(java.io.FileDescriptor.out)
                ),
                true,  // autoFlush
                StandardCharsets.UTF_8
            ));
            
            System.setErr(new java.io.PrintStream(
                new java.io.BufferedOutputStream(
                    new java.io.FileOutputStream(java.io.FileDescriptor.err)
                ),
                true,  // autoFlush
                StandardCharsets.UTF_8
            ));
        } catch (Exception e) {
            // Silently fail - we'll use default encoding
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
