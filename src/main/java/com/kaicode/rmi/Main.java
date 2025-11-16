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
 */
@CommandLine.Command(name = "rmi", description = "Repository Maintainability Index - Automated GitHub repository quality assessment", version = "1.0.0", mixinStandardHelpOptions = true, subcommands = {
        AnalyzeCommand.class })
public class Main implements Runnable {

    /**
     * Static initializer to ensure UTF-8 encoding setup before framework initialization.
     * <p>
     * This block must execute before any logging framework (Logback/SLF4J)
     * initializes to prevent encoding issues with international characters
     * in repository analysis output. The EncodingInitializer configures
     * system streams for proper Unicode handling.
     * <p>
     * Executed automatically before main() when the class is loaded.
     *
     * @see EncodingInitializer#ensureInitialized()
     */
    static {
        EncodingInitializer.ensureInitialized();
    }

    /**
     * Application entry point that parses command-line arguments and executes commands.
     * <p>
     * This method initializes Picocli command-line parsing, creates the root command
     * instance, executes the specified subcommand based on arguments, and returns
     * the appropriate exit code. All exceptions are caught and converted to
     * meaningful exit codes.
     * <p>
     * The command-line interface supports:
     * <ul>
     *   <li>Subcommands for specific operations (analyze, help, version)</li>
     *   <li>Standard options (--help, --version)</li>
     *   <li>Mixin standard help options for consistent CLI experience</li>
     * </ul>
     *
     * @param args command-line arguments passed from the operating system,
     *             may be empty array but must not be null
     * @see CommandLine#execute(String...)
     * @see System#exit(int)
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    /**
     * Executes the root command when no subcommand is specified.
     * <p>
     * This method displays usage information and helpful hints to guide
     * users toward the correct subcommand. It does not perform any analysis
     * itself, but provides clear instructions on how to get started.
     * <p>
     * Output is written to standard output stream and includes:
     * <ul>
     *   <li>Primary usage example with repository analysis</li>
     *   <li>Instructions to access help documentation</li>
     * </ul>
     * <p>
     * This method is thread-safe and can be called from any thread.
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
