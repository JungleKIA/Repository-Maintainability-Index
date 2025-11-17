package com.kaicode.rmi.util;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Critical encoding initializer for UTF-8 console streams.
 * <p>
 * <strong>⚠️ CRITICAL CLASS LOADING REQUIREMENT:</strong><br>
 * This class MUST be loaded BEFORE any logging framework initialization (Logback/SLF4J).
 * The static initializer reconfigures System.out and System.err as UTF-8 streams
 * without requiring the -Dfile.encoding=UTF-8 JVM flag.
 * <p>
 * Key behavioral aspects:
 * <ul>
 *   <li>Static initializer runs automatically on class loading</li>
 *   <li>Wraps System.out/System.err with UTF-8 PrintStreams</li>
 *   <li>Configures Windows console code page to UTF-8 (65001)</li>
 *   <li>Fail-safe design with graceful degradation</li>
 *   <li>No external dependencies or side effects</li>
 * </ul>
 * <p>
 * Proper installation requires early class loading, ideally as the first imported
 * class in the application's main method or as the first statement in main().
 * <p>
 * Core problem solved:
 * Windows console environments default to non-Unicode code pages (CP437, CP1252)
 * that cannot display Unicode box-drawing characters used in table formatting.
 * This initializer forces UTF-8 mode for proper visual output.
 * <p>
 * Before application startup:
 * <pre>{@code
 * import com.kaicode.rmi.util.EncodingInitializer;
 *
 * public class Main {
 *     public static void main(String[] args) {
 *         EncodingInitializer.ensureInitialized(); // Triggers UTF-8 setup
 *
 *         // Now logging frameworks will use UTF-8 streams
 *         Logger logger = LoggerFactory.getLogger(Main.class);
 *         // All console output now supports full Unicode
 *     }
 * }
 * }</pre>
 * <p>
 * Alternative implicit trigger (through static initializer):
 * <pre>{@code
 * import com.kaicode.rmi.util.EncodingInitializer;
 *
 * public class Application {
 *     // Class loading triggers EncodingInitializer static block
 *     private static final Logger logger = LoggerFactory.getLogger(Application.class);
 *     // EncodingInitializer is loaded first, before Logger initialization
 * }
 * }</pre>
 *
 * @since 1.0
 * @see EncodingHelper#setupUTF8ConsoleStreams()
 * @see UTF8Console
 */
public class EncodingInitializer {

    private static boolean initialized = false;

    static {
        // ⚠️ CRITICAL: This static block MUST run before Logback/SLF4J initialization
        // It reconfigures console streams to UTF-8 without -Dfile.encoding=UTF-8 JVM flag
        initializeEncoding();
    }

    /**
     * Initialize UTF-8 encoding for console streams.
     * <p>
     * Core method that wraps System.out and System.err with UTF-8 PrintStreams.
     * This approach works regardless of JVM file.encoding setting by directly
     * modifying the runtime console output streams.
     * <p>
     * Process sequence:
     * <ol>
     *   <li>Set Windows console to UTF-8 code page (chcp 65001)</li>
     *   <li>Flush existing output buffers to prevent corruption</li>
     *   <li>Create UTF-8 PrintStream wrappers for System.out/System.err</li>
     *   <li>Replace default streams with UTF-8 enabled versions</li>
     * </ol>
     * <p>
     * Idempotent operation - safe to call multiple times.
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
     * Force initialization of encoding configuration.
     * <p>
     * Explicit trigger method for initialization. Accessing this method
     * ensures the static initializer has run and console streams are UTF-8 enabled.
     * <p>
     * Use this method when implicit triggering through class loading order
     * is not predictable or when explicit early initialization is required.
     * <p>
     * Thread-safe idempotent operation.
     */
    public static void ensureInitialized() {
        // Just accessing this class triggers the static initializer
        // This method exists to provide an explicit way to trigger initialization
    }

    /**
     * Checks if the current platform is Windows.
     * <p>
     * Platform detection used to determine if console code page changes are necessary.
     * Examines the "os.name" system property for Windows variant detection.
     *
     * @return true if running on Windows platform, false otherwise
     */
    private static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }
}
