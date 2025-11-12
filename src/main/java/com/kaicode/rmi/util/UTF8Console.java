package com.kaicode.rmi.util;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * UTF-8 console output wrapper for Windows/GitBash compatibility.
 * 
 * This class provides a reliable way to output UTF-8 text to console,
 * especially for Windows GitBash where standard System.out may not handle
 * Unicode characters correctly.
 */
public class UTF8Console {
    
    private static PrintWriter out = null;
    private static PrintWriter err = null;
    
    /**
     * Initializes UTF-8 console writers.
     * Should be called early in application startup.
     */
    public static void initialize() {
        try {
            // Create UTF-8 PrintWriters wrapping System.out and System.err
            out = new PrintWriter(
                new OutputStreamWriter(System.out, StandardCharsets.UTF_8), 
                true  // autoFlush
            );
            
            err = new PrintWriter(
                new OutputStreamWriter(System.err, StandardCharsets.UTF_8),
                true  // autoFlush
            );
        } catch (Exception e) {
            // Fallback to standard System.out/err
            out = null;
            err = null;
        }
    }
    
    /**
     * Prints text to stdout using UTF-8 encoding.
     * 
     * @param text the text to print
     */
    public static void print(String text) {
        if (out != null) {
            out.print(text);
            out.flush();
        } else {
            System.out.print(text);
        }
    }
    
    /**
     * Prints text to stdout with newline using UTF-8 encoding.
     * 
     * @param text the text to print
     */
    public static void println(String text) {
        if (out != null) {
            out.println(text);
            out.flush();
        } else {
            System.out.println(text);
        }
    }
    
    /**
     * Prints empty line to stdout.
     */
    public static void println() {
        if (out != null) {
            out.println();
            out.flush();
        } else {
            System.out.println();
        }
    }
    
    /**
     * Prints error text to stderr using UTF-8 encoding.
     * 
     * @param text the error text to print
     */
    public static void printErr(String text) {
        if (err != null) {
            err.print(text);
            err.flush();
        } else {
            System.err.print(text);
        }
    }
    
    /**
     * Prints error text to stderr with newline using UTF-8 encoding.
     * 
     * @param text the error text to print
     */
    public static void printlnErr(String text) {
        if (err != null) {
            err.println(text);
            err.flush();
        } else {
            System.err.println(text);
        }
    }
    
    /**
     * Flushes the output buffers.
     */
    public static void flush() {
        if (out != null) {
            out.flush();
        }
        if (err != null) {
            err.flush();
        }
    }
}
