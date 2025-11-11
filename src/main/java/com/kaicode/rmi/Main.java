package com.kaicode.rmi;

import com.kaicode.rmi.cli.AnalyzeCommand;
import com.kaicode.rmi.util.EncodingHelper;
import picocli.CommandLine;

@CommandLine.Command(
        name = "rmi",
        description = "Repository Maintainability Index - Automated GitHub repository quality assessment",
        version = "1.0.0",
        mixinStandardHelpOptions = true,
        subcommands = {AnalyzeCommand.class}
)
public class Main implements Runnable {

    public static void main(String[] args) {
        // Configure UTF-8 encoding for console output (especially important for Windows/GitBash)
        EncodingHelper.setupUTF8ConsoleStreams();
        
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        System.out.println("Use 'rmi analyze <owner/repo>' to analyze a repository.");
        System.out.println("Use 'rmi --help' for more information.");
    }
}
