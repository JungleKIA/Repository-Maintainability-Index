package com.kaicode.rmi.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * UTF-8 console output wrapper for Windows/GitBash compatibility.
 * 
 * This class provides a reliable way to output UTF-8 text to console,
 * especially for Windows GitBash where standard System.out may not handle
 * Unicode characters correctly.
 * 
 * Key approach: Write raw UTF-8 bytes directly to System.out, bypassing
 * all Java encoding layers that might corrupt the output.
 */
public class UTF8Console {
    
    /**
     * Initializes UTF-8 console writers.
     * For this implementation, no initialization is needed as we write directly to System.out.
     */
    public static void initialize() {
        // No-op: We write directly to System.out as raw UTF-8 bytes
    }
    
    /**
     * Prints text to stdout using UTF-8 encoding.
     * Writes raw UTF-8 bytes directly to System.out.
     * 
     * @param text the text to print
     */
    public static void print(String text) {
        try {
            if (text != null) {
                // Convert to UTF-8 bytes and write directly
                byte[] utf8Bytes = text.getBytes(StandardCharsets.UTF_8);
                System.out.write(utf8Bytes);
                System.out.flush();
            }
        } catch (IOException e) {
            // Fallback to standard println if direct write fails
            System.out.print(text);
        }
    }
    
    /**
     * Prints text to stdout with newline using UTF-8 encoding.
     * Writes raw UTF-8 bytes directly to System.out.
     * 
     * @param text the text to print
     */
    public static void println(String text) {
        try {
            if (text != null) {
                // Convert to UTF-8 bytes and write directly
                byte[] utf8Bytes = (text + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
                System.out.write(utf8Bytes);
                System.out.flush();
            }
        } catch (IOException e) {
            // Fallback to standard println if direct write fails
            System.out.println(text);
        }
    }
    
    /**
     * Prints empty line to stdout.
     */
    public static void println() {
        try {
            byte[] newline = System.lineSeparator().getBytes(StandardCharsets.UTF_8);
            System.out.write(newline);
            System.out.flush();
        } catch (IOException e) {
            System.out.println();
        }
    }
    
    /**
     * Prints error text to stderr using UTF-8 encoding.
     * Writes raw UTF-8 bytes directly to System.err.
     * 
     * @param text the error text to print
     */
    public static void printErr(String text) {
        try {
            if (text != null) {
                byte[] utf8Bytes = text.getBytes(StandardCharsets.UTF_8);
                System.err.write(utf8Bytes);
                System.err.flush();
            }
        } catch (IOException e) {
            System.err.print(text);
        }
    }
    
    /**
     * Prints error text to stderr with newline using UTF-8 encoding.
     * Writes raw UTF-8 bytes directly to System.err.
     * 
     * @param text the error text to print
     */
    public static void printlnErr(String text) {
        try {
            if (text != null) {
                byte[] utf8Bytes = (text + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
                System.err.write(utf8Bytes);
                System.err.flush();
            }
        } catch (IOException e) {
            System.err.println(text);
        }
    }
    
    /**
     * Flushes the output buffers.
     */
    public static void flush() {
        System.out.flush();
        System.err.flush();
    }
}
