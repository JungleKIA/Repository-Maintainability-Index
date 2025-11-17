package com.kaicode.rmi.util;

/**
 * Legacy UTF-8 console output wrapper (DEPRECATED).
 * <p>
 * <strong>⚠️ DEPRECATED:</strong> This class is deprecated and should not be used.
 * It has been replaced by a more robust and simpler approach using {@link EncodingHelper}.
 * <p>
 * Historical context: This class was the initial solution for UTF-8 console output issues
 * in Windows/GitBash environments. It wrapped System.out/System.err with PrintWriter instances,
 * but this approach had limitations:
 * <ul>
 *   <li>Complex initialization requirements</li>
 *   <li>Not compatible with logging frameworks that cached streams</li>
 *   <li>Required calling non-standard methods instead of standard System methods</li>
 *   <li>Difficult to integrate with third-party libraries</li>
 * </ul>
 * <p>
 * Modern replacement approach (RECOMMENDED):
 * <ol>
 *   <li>Call {@link EncodingHelper#setupUTF8ConsoleStreams()} once at application start</li>
 *   <li>Use {@link EncodingHelper#cleanTextForWindows(String)} on output that may contain mojibake</li>
 *   <li>Use standard System.out/System.err methods for all printing</li>
 * </ol>
 * <p>
 * Benefits of the new approach:
 * <ul>
 *   <li>✅ Transparent operation - no API changes required</li>
 *   <li>✅ Logging framework compatibility (Logback auto-rediscovery)</li>
 *   <li>✅ Standard Java API usage</li>
 *   <li>✅ Better error handling and fallbacks</li>
 *   <li>✅ Works with third-party libraries out of the box</li>
 * </ul>
 * <p>
 * Migration example:
 * <pre>{@code
 * // OLD (deprecated):
 * UTF8Console.initialize();
 * UTF8Console.println("Hello, 世界!"); // Non-standard API
 *
 * // NEW (recommended):
 * EncodingHelper.setupUTF8ConsoleStreams(); // One-time setup
 * System.out.println(EncodingHelper.cleanTextForWindows("Hello, 世界!")); // Standard API
 * }</pre>
 * <p>
 * This class is preserved for backward compatibility but will be removed in a future version.
 * All new code should use EncodingHelper directly.
 *
 * @deprecated Use {@link EncodingHelper#setupUTF8ConsoleStreams()} and standard System.out methods instead
 * @see EncodingHelper#setupUTF8ConsoleStreams()
 * @see EncodingHelper#cleanTextForWindows(String)
 */
@Deprecated
public class UTF8Console {

    /**
     * Initializes UTF-8 console writers (LEGACY METHOD).
     * <p>
     * This method is a no-op and exists only for backward compatibility.
     * UTF-8 initialization is now handled by {@link EncodingHelper#setupUTF8ConsoleStreams()}.
     * <p>
     * Call {@code EncodingHelper.setupUTF8ConsoleStreams()} instead.
     *
     * @deprecated Use {@link EncodingHelper#setupUTF8ConsoleStreams()} instead
     */
    @Deprecated
    public static void initialize() {
        // No-op: Initialization now done in EncodingHelper
    }

    /**
     * Prints text to stdout (LEGACY METHOD).
     * <p>
     * Replaced by standard {@link System#out} with text cleaning.
     * <p>
     * Migration: {@code System.out.print(EncodingHelper.cleanTextForWindows(text))}
     *
     * @param text the text to print
     * @deprecated Use {@code System.out.print(EncodingHelper.cleanTextForWindows(text))}
     */
    @Deprecated
    public static void print(String text) {
        System.out.print(text);
    }

    /**
     * Prints text to stdout with newline (LEGACY METHOD).
     * <p>
     * Replaced by standard {@link System#out} with text cleaning.
     * <p>
     * Migration: {@code System.out.println(EncodingHelper.cleanTextForWindows(text))}
     *
     * @param text the text to print
     * @deprecated Use {@code System.out.println(EncodingHelper.cleanTextForWindows(text))}
     */
    @Deprecated
    public static void println(String text) {
        System.out.println(text);
    }

    /**
     * Prints empty line to stdout (LEGACY METHOD).
     * <p>
     * Replaced by standard {@link System#out}.
     * <p>
     * Migration: {@code System.out.println()}
     *
     * @deprecated Use {@link System#out}.{@link java.io.PrintStream#println() println()}
     */
    @Deprecated
    public static void println() {
        System.out.println();
    }

    /**
     * Prints error text to stderr (LEGACY METHOD).
     * <p>
     * Replaced by standard {@link System#err}.
     * <p>
     * Migration: {@code System.err.print(text)}
     *
     * @param text the error text to print
     * @deprecated Use {@link System#err}.{@link java.io.PrintStream#print(String) print(text)}
     */
    @Deprecated
    public static void printErr(String text) {
        System.err.print(text);
    }

    /**
     * Prints error text to stderr with newline (LEGACY METHOD).
     * <p>
     * Replaced by standard {@link System#err}.
     * <p>
     * Migration: {@code System.err.println(text)}
     *
     * @param text the error text to print
     * @deprecated Use {@link System#err}.{@link java.io.PrintStream#println(String) println(text)}
     */
    @Deprecated
    public static void printlnErr(String text) {
        System.err.println(text);
    }

    /**
     * Flushes the output buffers (LEGACY METHOD).
     * <p>
     * Replaced by direct stream access.
     * <p>
     * Migration: {@code System.out.flush(); System.err.flush()}
     *
     * @deprecated Use {@link System#out}.{@link java.io.PrintStream#flush() flush()} and {@link System#err}.{@link java.io.PrintStream#flush() flush()}
     */
    @Deprecated
    public static void flush() {
        System.out.flush();
        System.err.flush();
    }
}
