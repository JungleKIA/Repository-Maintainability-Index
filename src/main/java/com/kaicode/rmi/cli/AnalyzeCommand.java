package com.kaicode.rmi.cli;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.MaintainabilityReport;
import com.kaicode.rmi.service.MaintainabilityService;
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

    @Option(names = {"-t", "--token"}, description = "GitHub API token (optional, for higher rate limits)")
    private String token;

    @Option(names = {"-f", "--format"}, description = "Output format: text or json (default: text)")
    private String format = "text";

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

            GitHubClient client = new GitHubClient(token);
            MaintainabilityService service = new MaintainabilityService(client);

            System.out.println("Analyzing repository: " + repository);
            System.out.println("This may take a moment...\n");

            MaintainabilityReport report = service.analyze(owner, repo);

            ReportFormatter formatter = new ReportFormatter();
            ReportFormatter.OutputFormat outputFormat = format.equalsIgnoreCase("json")
                    ? ReportFormatter.OutputFormat.JSON
                    : ReportFormatter.OutputFormat.TEXT;

            String output = formatter.format(report, outputFormat);
            System.out.println(output);

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
