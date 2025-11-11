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
     * Note: This may not work in all environments, especially in Windows.
     */
    public static void configureConsoleEncoding() {
        try {
            // Try to set UTF-8 for System.out
            System.setOut(new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            // Silently fail - we'll use other methods
        }
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

    /**
     * Checks if we're running in Git Bash or similar environment on Windows.
     * These environments often have issues with box-drawing characters.
     * 
     * @return true if running in Git Bash on Windows, false otherwise
     */
    public static boolean isGitBashOnWindows() {
        if (!isWindows()) {
            return false;
        }
        
        // Check for common Git Bash environment variables
        String msystem = System.getenv("MSYSTEM");
        String term = System.getenv("TERM");
        
        return (msystem != null && (msystem.contains("MINGW") || msystem.contains("MSYS"))) ||
               (term != null && term.contains("xterm"));
    }

    /**
     * Determines if we should use ASCII-safe characters instead of Unicode box-drawing characters.
     * 
     * @return true if ASCII characters should be used, false otherwise
     */
    public static boolean shouldUseAsciiCharacters() {
        return isWindows() || isGitBashOnWindows();
    }

    /**
     * Gets the appropriate horizontal line character for the current environment.
     * 
     * @return '=' for ASCII-safe environments, '═' for full Unicode support
     */
    public static String getDoubleHorizontalLine() {
        return shouldUseAsciiCharacters() ? "=" : "═";
    }

    /**
     * Gets the appropriate single horizontal line character for the current environment.
     * 
     * @return '-' for ASCII-safe environments, '─' for full Unicode support
     */
    public static String getSingleHorizontalLine() {
        return shouldUseAsciiCharacters() ? "-" : "─";
    }

    /**
     * Gets the appropriate bullet point character for the current environment.
     * 
     * @return '*' for ASCII-safe environments, '▪' for full Unicode support
     */
    public static String getBulletPoint() {
        return shouldUseAsciiCharacters() ? "*" : "▪";
    }

    /**
     * Gets the appropriate box top-left corner for the current environment.
     * 
     * @return '+' for ASCII-safe environments, '┌' for full Unicode support
     */
    public static String getBoxTopLeft() {
        return shouldUseAsciiCharacters() ? "+" : "┌";
    }

    /**
     * Gets the appropriate box top-right corner for the current environment.
     * 
     * @return '+' for ASCII-safe environments, '┐' for full Unicode support
     */
    public static String getBoxTopRight() {
        return shouldUseAsciiCharacters() ? "+" : "┐";
    }

    /**
     * Gets the appropriate box bottom-left corner for the current environment.
     * 
     * @return '+' for ASCII-safe environments, '└' for full Unicode support
     */
    public static String getBoxBottomLeft() {
        return shouldUseAsciiCharacters() ? "+" : "└";
    }

    /**
     * Gets the appropriate box bottom-right corner for the current environment.
     * 
     * @return '+' for ASCII-safe environments, '┘' for full Unicode support
     */
    public static String getBoxBottomRight() {
        return shouldUseAsciiCharacters() ? "+" : "┘";
    }

    /**
     * Gets the appropriate box vertical line for the current environment.
     * 
     * @return '|' for ASCII-safe environments, '│' for full Unicode support
     */
    public static String getBoxVerticalLine() {
        return shouldUseAsciiCharacters() ? "|" : "│";
    }

    /**
     * Gets the appropriate box middle-left for the current environment.
     * 
     * @return '+' for ASCII-safe environments, '├' for full Unicode support
     */
    public static String getBoxMiddleLeft() {
        return shouldUseAsciiCharacters() ? "+" : "├";
    }

    /**
     * Gets the appropriate box middle-right for the current environment.
     * 
     * @return '+' for ASCII-safe environments, '┤' for full Unicode support
     */
    public static String getBoxMiddleRight() {
        return shouldUseAsciiCharacters() ? "+" : "┤";
    }

    /**
     * Creates a horizontal separator line with the specified character and length.
     * 
     * @param character the character to repeat
     * @param length the length of the line
     * @return the separator line
     */
    public static String createSeparatorLine(String character, int length) {
        return character.repeat(length);
    }
}
