package com.kaicode.rmi.cli;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MaintainabilityReport;
import com.kaicode.rmi.service.MaintainabilityService;
import com.kaicode.rmi.util.EnvironmentLoader;
import com.kaicode.rmi.util.ReportFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "analyze",
        description = "Analyze a GitHub repository and calculate its maintainability index",
        mixinStandardHelpOptions = true)
public class AnalyzeCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "GitHub repository in format: owner/repo")
    private String repository;

    @Option(names = {"-t", "--token"}, description = "GitHub API token (optional, for higher rate limits, can also be set via GITHUB_TOKEN env var)")
    private String token;

    @Option(names = {"-f", "--format"}, description = "Output format: text or json (default: text)")
    private String format = "text";

    @Option(names = {"-l", "--llm"}, description = "Enable LLM analysis (requires API key in OPENROUTER_API_KEY env var)")
    private boolean enableLLM = false;

    @Option(names = {"-m", "--model"}, description = "LLM model to use (can also be set via OPENROUTER_MODEL env var, default: openai/gpt-oss-20b:free)")
    private String llmModel = "openai/gpt-oss-20b:free";

    @Override
    public Integer call() {
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

            System.out.println("Analyzing repository: " + repository);
            System.out.println("This may take a moment...\n");

            MaintainabilityReport report = service.analyze(owner, repo);

            if (enableLLM) {
                String apiKey = EnvironmentLoader.getEnv("OPENROUTER_API_KEY");
                if (apiKey == null || apiKey.isEmpty()) {
                    System.err.println("Warning: OPENROUTER_API_KEY not set, LLM analysis disabled");
                    enableLLM = false;
                }
            }

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

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }
}
