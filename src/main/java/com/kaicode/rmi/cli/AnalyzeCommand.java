package com.kaicode.rmi.cli;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MaintainabilityReport;
import com.kaicode.rmi.service.MaintainabilityService;
import com.kaicode.rmi.util.EnvironmentLoader;
import com.kaicode.rmi.util.ReportFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * CLI command for comprehensive GitHub repository maintainability analysis.
 * <p>
 * This command orchestrates end-to-end repository analysis including traditional
 * quantitative metrics (activity, documentation, commit quality, etc.) and optional
 * AI-powered qualitative insights through Large Language Model integration.
 * <p>
 * Command capabilities:
 * <ul>
 *   <li><strong>Repository Analysis</strong>: Automated quality assessment using 6 metrics</li>
 *   <li><strong>LLM Integration</strong>: Optional AI-powered feedback and recommendations</li>
 *   <li><strong>Flexible Output</strong>: Text and JSON formats for human/machine consumption</li>
 *   <li><strong>Authentication</strong>: GitHub token support for API rate limit optimization</li>
 *   <li><strong>Configuration</strong>: Environment variable and CLI parameter support</li>
 * </ul>
 * <p>
 * Analysis workflow:
 * <pre>{@code
 * ┌─────────────┐    ┌─────────────┐    ┌─────────────────┐
 * │ Parse Args  │───▶│ Create     │───▶│ Maintainability │
 * │ (owner/repo)│    │ Service    │    │   Analysis      │
 * └─────────────┘    └─────────────┘    └─────────────────┘
 *                                                       │
 *                                                       ▼
 * ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
 * │ Optional    │───▶│ LLM         │───▶│ Format      │
 * │ LLM         │    │ Analysis    │    │ Results     │
 * │ Analysis    │    │ (AI)        │    │ (Text/JSON) │
 * └─────────────┘    └─────────────┘    └─────────────┘
 * }</pre>
 * <p>
 * CLI usage examples:
 * <pre>{@code
 * # Basic text analysis
 * rmi analyze microsoft/vscode
 *
 * # JSON output for CI/CD integration
 * rmi analyze facebook/react --format json --token $GITHUB_TOKEN
 *
 * # Full LLM-powered analysis
 * rmi analyze angular/angular --llm --model openai/gpt-4
 *
 * # Environmental configuration
 * GITHUB_TOKEN=ghp_123 export GITHUB_TOKEN
 * OPENAI_API_KEY=sk-456 export OPENAI_API_KEY
 * rmi analyze myorg/myrepo --llm
 * }</pre>
 * <p>
 * Configuration priority (highest to lowest):
 * <ol>
 *   <li>CLI command-line parameters</li>
 *   <li>Environment variables</li>
 *   <li>Built-in sensible defaults</li>
 * </ol>
 * <p>
 * Error handling provides clear exit codes and informative error messages
 * for reliable scripting and automation integration.
 *
 * @since 1.0
 * @see MaintainabilityService
 * @see GitHubClient
 * @see com.kaicode.rmi.llm.LLMAnalyzer
 * @see ReportFormatter
 */
@Command(name = "analyze",
        description = "Analyze a GitHub repository and calculate its maintainability index",
        mixinStandardHelpOptions = true)
public class AnalyzeCommand implements Callable<Integer> {

    /**
     * GitHub repository specification in owner/repo format.
     * <p>
     * The primary command parameter specifying which repository to analyze.
     * Must follow the standard GitHub format with forward slash separation.
     * The repository will be validated before analysis begins.
     * <p>
     * Examples: "microsoft/vscode", "facebook/react", "apache/kafka"
     */
    @Parameters(index = "0", description = "GitHub repository in format: owner/repo")
    private String repository;

    /**
     * GitHub API authentication token parameter.
     * <p>
     * Personal access token for authenticated GitHub API requests.
     * Eliminates rate limiting restrictions (5000 vs 60 requests/hour).
     * If not provided, falls back to GITHUB_TOKEN environment variable.
     * Token should have basic read access to public repositories.
     * <p>
     * CLI override takes precedence over environment configuration.
     * <p>
     * Token generation: GitHub Settings → Developer settings → Personal access tokens
     */
    @Option(names = {"-t", "--token"}, description = "GitHub API token (optional, for higher rate limits, can also be set via GITHUB_TOKEN env var)")
    private String token;

    /**
     * Output format specification.
     * <p>
     * Controls the response serialization format for different consumption contexts.
     * Text format provides human-readable visual reports with emoji and progress bars.
     * JSON format provides machine-readable structured data for integration.
     * <p>
     * Supported values: "text" (default), "json"
     * <p>
     * Recommendation: Use "text" for interactive CLI, "json" for CI/CD pipelines
     */
    @Option(names = {"-f", "--format"}, description = "Output format: text or json (default: text)")
    private String format = "text";

    /**
     * Large Language Model analysis enablement flag.
     * <p>
     * Activates AI-powered qualitative analysis in addition to quantitative metrics.
     * LLM analysis provides human-readable insights and specific recommendations
     * beyond scoring algorithms. Requires API key configuration.
     * <p>
     * Prerequisites: OPENROUTER_API_KEY environment variable must be set
     * Warning displayed if key missing - analysis continues without LLM component
     * <p>
     * Impact: Significantly increases analysis depth but requires API calls
     */
    @Option(names = {"-l", "--llm"}, description = "Enable LLM analysis (requires API key in OPENROUTER_API_KEY env var)")
    private boolean enableLLM = false;

    /**
     * LLM model specification for AI analysis.
     * <p>
     * Defines which LLM model to use from available OpenRouter options.
     * Influences analysis quality, token cost, and response speed.
     * <p>
     * If not specified via CLI, uses OPENROUTER_MODEL environment variable,
     * then falls back to default model "openai/gpt-oss-20b:free"
     * <p>
     * Popular options:
     * <ul>
     *   <li>openai/gpt-oss-20b:free (default, cost-free)</li>
     *   <li>openai/gpt-3.5-turbo (fast, balanced)</li>
     *   <li>openai/gpt-4 (highest quality, expensive)</li>
     *   <li>anthropic/claude-3-haiku (fast, high quality)</li>
     * </ul>
     */
    @Option(names = {"-m", "--model"}, description = "LLM model to use (can also be set via OPENROUTER_MODEL env var, default: openai/gpt-oss-20b:free)")
    private String llmModel = "openai/gpt-oss-20b:free";

    /**
     * Quiet mode flag to suppress all output except errors.
     * <p>
     * Completely suppresses stdout output including analysis results and progress messages.
     * Only error messages are displayed on stderr for debugging purposes.
     * Analysis still runs normally - this is useful for scripts that only need exit codes.
     *
     * <p>
     * Use cases:
     * <ul>
     *   <li>Script integration where only exit code matters</li>
     *   <li>CI/CD pipelines with silent operation</li>
     *   <li>Automated processing without console clutter</li>
     *   <li>Bulk operations where results are not needed immediately</li>
     * </ul>
     */
    @Option(names = {"-q", "--quiet"}, description = "Suppress all output except errors (useful for scripting where only exit code matters)")
    private boolean quiet = false;

    /**
     * Command execution entry point coordinating complete repository analysis workflow.
     * <p>
     * This method orchestrates the entire analysis pipeline from argument validation
     * through result formatting. Implements Picocli Callable pattern for proper
     * CLI integration and exit code management.
     * <p>
     * Execution stages:
     * <ol>
     *   <li><strong>Validation</strong>: Parse and validate repository argument</li>
     *   <li><strong>Security</strong>: Load authentication tokens from CLI/env</li>
     *   <li><strong>Dependencies</strong>: Initialize GitHub client and analysis service</li>
     *   <li><strong>Core Analysis</strong>: Execute metric calculations across all categories</li>
     *   <li><strong>LLM Enhancement</strong>: Optionally run AI-powered insights (if enabled)</li>
     *   <li><strong>Formatting</strong>: Serialize results in requested format (text/JSON)</li>
     *   <li><strong>Output</strong>: Stream results to stdout using UTF-8 encoding</li>
     * </ol>
     * <p>
     * Error handling strategy:
     * <ul>
     *   <li>Input validation errors → clear guidance messages → exit code 1</li>
     *   <li>Network/API failures → detailed error messages → exit code 1</li>
     *   <li>LLM setup issues → warning messages → continue without AI → exit code 0</li>
     *   <li>Formatting/output errors → abort execution → exit code 1</li>
     * </ul>
     * <p>
     * Performance characteristics:
     * <ul>
     *   <li>Base metrics: ~3-5 seconds depending on repository size</li>
     *   <li>LLM analysis: +15-60 seconds depending on model and content</li>
     *   <li>Memory usage: Minimal, streaming API responses</li>
     *   <li>Error recovery: Fast failure for invalid inputs</li>
     * </ul>
     *
     * @return exit code: 0 for success, 1 for any error condition
     * @throws Exception if repository access, file reading, or any other errors occur during analysis
     */
    @Override
    public Integer call() throws Exception {
        try {
            String[] parts = repository.split("/");
            if (parts.length != 2) {
                System.err.println("Error: Repository must be in format 'owner/repo'");
                return 1;
            }

            String owner = parts[0];
            String repo = parts[1];

            // Use token from command line or environment variable
            String githubToken = token != null ? token : EnvironmentLoader.getEnv("GITHUB_TOKEN");
            GitHubClient client = new GitHubClient(githubToken);
            MaintainabilityService service = new MaintainabilityService(client);

            // Use LLM model from command line or environment variable
            String model = EnvironmentLoader.getEnv("OPENROUTER_MODEL", llmModel);

            if (!quiet) {
                System.out.println("Analyzing repository: " + repository);
                System.out.println("This may take a moment...\n");
            }

            MaintainabilityReport report = service.analyze(owner, repo);

            if (enableLLM) {
                String apiKey = EnvironmentLoader.getEnv("OPENROUTER_API_KEY");
                if (apiKey == null || apiKey.isEmpty()) {
                    System.err.println("Warning: OPENROUTER_API_KEY not set, LLM analysis disabled");
                    enableLLM = false;
                }
            }

            if (!quiet) {
                if (enableLLM && format.equalsIgnoreCase("text")) {
                    String apiKey = EnvironmentLoader.getEnv("OPENROUTER_API_KEY");
                    com.kaicode.rmi.llm.LLMClient llmClient = new com.kaicode.rmi.llm.LLMClient(apiKey, model);
                    com.kaicode.rmi.llm.LLMAnalyzer llmAnalyzer = new com.kaicode.rmi.llm.LLMAnalyzer(llmClient);

                    System.out.println("Running LLM analysis...\n");
                    com.kaicode.rmi.model.LLMAnalysis llmAnalysis = llmAnalyzer.analyze(client, owner, repo);

                    com.kaicode.rmi.util.LLMReportFormatter llmFormatter = new com.kaicode.rmi.util.LLMReportFormatter();
                    String output = llmFormatter.formatWithLLM(report, llmAnalysis);

                    // Output is already UTF-8 encoded by System.out (configured in Main)
                    System.out.println(output);
                } else {
                    ReportFormatter formatter = new ReportFormatter();
                    ReportFormatter.OutputFormat outputFormat = format.equalsIgnoreCase("json")
                            ? ReportFormatter.OutputFormat.JSON
                            : ReportFormatter.OutputFormat.TEXT;

                    String output = formatter.format(report, outputFormat);

                    // Output is already UTF-8 encoded by System.out (configured in Main)
                    System.out.println(output);
                }
            } else {
                // In quiet mode, analysis runs but output is suppressed
                // This allows scripts to check exit codes without visual clutter
            }

            return 0;
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
}
