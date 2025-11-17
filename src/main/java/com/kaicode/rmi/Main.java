package com.kaicode.rmi;

import com.kaicode.rmi.cli.AnalyzeCommand;
import com.kaicode.rmi.util.EncodingInitializer;
import picocli.CommandLine;

/**
 * Main entry point for Repository Maintainability Index (RMI) CLI application.
 * <p>
 * This class serves as the root command for the RMI tool, providing:
 * <ul>
 *   <li>Main method to launch the application</li>
 *   <li>Root command handler with help and version information</li>
 *   <li>UTF-8 encoding initialization before framework startup</li>
 *   <li>Integration with Picocli command-line parsing</li>
 * </ul>
 * <p>
 * The application analyzes GitHub repositories to assess maintainability
 * and provide actionable insights for codebase improvement.
 * <p>
 * Architecture overview:
 * <pre>{@code
 * ┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐
 * │   Main      │    │ AnalyzeCommand  │    │ Maintainability │
 * │   (Entry)   │───▶│   (CLI Args)    │───▶│   Service       │
 * │             │    │                 │    │   (Business)    │
 * └─────────────┘    └─────────────────┘    └─────────────────┘
 *         │                                              │
 *         │                                              ▼
 *         │                                     ┌─────────────────┐
 *         │                                     │   Metrics       │
 *         │                                     │   Calculators   │
 *         │                                     └─────────────────┘
 *         │                                              │
 *         ▼                                              ▼
 * ┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐
 * │ Encoding    │    │   GitHub        │    │      LLM       │
 * │ Initializer │    │   Client        │    │   Analysis     │
 * │ (UTF-8)     │    │   (API)         │    │   (AI)         │
 * └─────────────┘    └─────────────────┘    └─────────────────┘
 * }</pre>
 * <p>
 * Usage examples:
 * <pre>{@code
 * # Analyze a repository
 * java -jar rmi.jar analyze microsoft/vscode
 *
 * # Show help
 * java -jar rmi.jar --help
 *
 * # Show version
 * java -jar rmi.jar --version
 * }</pre>
 *
 * @since 1.0
 * @see AnalyzeCommand
 * @see EncodingInitializer
 * @see com.kaicode.rmi.service.MaintainabilityService
 */
@CommandLine.Command(name = "rmi", description = "Repository Maintainability Index - Automated GitHub repository quality assessment", version = "1.0.0", mixinStandardHelpOptions = true, subcommands = {
        AnalyzeCommand.class })
public class Main implements Runnable {

    /**
     * Critical static initializer ensuring UTF-8 encoding setup before any framework initialization.
     * <p>
     * <strong>⚠️ CRITICAL EXECUTION TIMING:</strong><br>
     * This static block MUST execute before any logging framework (Logback/SLF4J)
     * initializes. Failure to execute before logging setup can result in mojibake
     * and corrupted Unicode characters in analysis output.
     * <p>
     * Execution sequence:
     * <ol>
     *   <li>Class loaded → static block executes immediately</li>
     *   <li>EncodingInitializer configures UTF-8 streams</li>
     *   <li>Logging frameworks initialize safely</li>
     *   <li>Application startup proceeds normally</li>
     * </ol>
     * <p>
     * Why static initialization matters:
     * Logback captures System.out during initialization. Without UTF-8 streams,
     * all console output (including repository analysis results) gets corrupted
     * with mojibake on Windows/GitBash systems.
     * <p>
     * Thread safety: Static initializer is guaranteed to execute exactly once
     * during class loading, regardless of multi-threaded access patterns.
     *
     * @see EncodingInitializer#ensureInitialized()
     * @see com.kaicode.rmi.util.EncodingInitializer
     */
    static {
        EncodingInitializer.ensureInitialized();
    }

    /**
     * Application entry point that bootstraps CLI parsing and command execution.
     * <p>
     * This method serves as the JVM entry point and coordinates the entire application
     * lifecycle. It initializes the Picocli command-line parsing framework, registers
     * subcommands, executes the appropriate command based on CLI arguments, and manages
     * proper JVM shutdown with appropriate exit codes.
     * <p>
     * Bootstrapping process:
     * <ol>
     *   <li>UTF-8 streams configured (static initializer)</li>
     *   <li>CommandLine instance created with Main command</li>
     *   <li>CLI arguments parsed and subcommand executed</li>
     *   <li>Exit code returned from subcommand execution</li>
     *   <li>JVM terminated with appropriate exit code</li>
     * </ol>
     * <p>
     * Error handling: All exceptions during execution are caught by Picocli's
     * execute() method and converted to meaningful exit codes (0=success, 1=parse error,
     * 2=execution error). This ensures predictable behavior for shell scripts.
     * <p>
     * The command-line interface supports:
     * <ul>
     *   <li><strong>analyze</strong>: Repository analysis subcommand</li>
     *   <li><strong>--help</strong>: Display usage information</li>
     *   <li><strong>--version</strong>: Show version information</li>
     *   <li><strong>Standard options</strong>: Auto-generated help and mixin options</li>
     * </ul>
     *
     * @param args command-line arguments passed from the operating system,
     *             may be empty array but must not be null. First argument
     *             determines subcommand (e.g., "analyze", "--help")
     * @see CommandLine#execute(String...)
     * @see System#exit(int)
     * @see AnalyzeCommand
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Root command execution handler when no subcommand is specified.
     * <p>
     * This method provides user guidance when the application is invoked without
     * specific subcommands. Instead of failing silently, it displays helpful
     * instructions for common use cases, making the tool more discoverable.
     * <p>
     * Behavior when called:
     * <ul>
     *   <li>Prints primary usage example (analyze command)</li>
     *   <li>Shows help access instruction</li>
     *   <li>Does not perform any analysis operations</li>
     *   <li>Returns control to Picocli framework</li>
     * </ul>
     * <p>
     * This approach improves user experience by providing immediate actionable
     * information rather than cryptic error messages.
     * <p>
     * Output format: Uses standard output stream for informative messages
     * that are designed to be human-readable and encouraging.
     * <p>
     * Thread safety: Method is stateless and thread-safe, using only primitive
     * operations and system output streams.
     *
     * @see AnalyzeCommand
     * @see CommandLine
     */
    @Override
    public void run() {
        System.out.println("Use 'rmi analyze <owner/repo>' to analyze a repository.");
        System.out.println("Use 'rmi --help' for more information.");
    }
}
