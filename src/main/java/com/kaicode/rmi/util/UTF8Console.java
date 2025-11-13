package com.kaicode.rmi.util;

/**
 * UTF-8 console output wrapper for Windows/GitBash compatibility.
 * 
 * This class has been replaced by a simpler approach:
 * 1. EncodingHelper.setupUTF8ConsoleStreams() wraps System.out/err with UTF-8
 * 2. EncodingHelper.cleanTextForWindows() fixes mojibake in output text
 * 3. Use standard System.out.println() for output
 * 
 * @deprecated Use EncodingHelper.cleanTextForWindows() instead
 */
@Deprecated
public class UTF8Console {
    
    /**
     * Initializes UTF-8 console writers.
     * This is now handled by EncodingHelper.setupUTF8ConsoleStreams().
     * 
     * @deprecated No longer needed
     */
    @Deprecated
    public static void initialize() {
        // No-op: Initialization now done in EncodingHelper
    }
    
    /**
     * Prints text to stdout.
     * Use standard System.out.print() instead.
     * 
     * @param text the text to print
     * @deprecated Use System.out.print(EncodingHelper.cleanTextForWindows(text))
     */
    @Deprecated
    public static void print(String text) {
        System.out.print(text);
    }
    
    /**
     * Prints text to stdout with newline.
     * Use standard System.out.println() instead.
     * 
     * @param text the text to print
     * @deprecated Use System.out.println(EncodingHelper.cleanTextForWindows(text))
     */
    @Deprecated
    public static void println(String text) {
        System.out.println(text);
    }
    
    /**
     * Prints empty line to stdout.
     * 
     * @deprecated Use System.out.println()
     */
    @Deprecated
    public static void println() {
        System.out.println();
    }
    
    /**
     * Prints error text to stderr.
     * 
     * @param text the error text to print
     * @deprecated Use System.err.print(text)
     */
    @Deprecated
    public static void printErr(String text) {
        System.err.print(text);
    }
    
    /**
     * Prints error text to stderr with newline.
     * 
     * @param text the error text to print
     * @deprecated Use System.err.println(text)
     */
    @Deprecated
    public static void printlnErr(String text) {
        System.err.println(text);
    }
    
    /**
     * Flushes the output buffers.
     * 
     * @deprecated Use System.out.flush() directly
     */
    @Deprecated
    public static void flush() {
        System.out.flush();
        System.err.flush();
    }
}
