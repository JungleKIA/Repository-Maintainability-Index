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
        // Set up UTF-8 encoding for console output streams.
        // This is crucial for ensuring correct character display on Windows/GitBash.
        try {
            EncodingHelper.setupUTF8ConsoleStreams();
        } catch (Exception e) {
            // Silently ignore if it fails. The application will continue with the default encoding.
        }

        // Reconfigure java.util.logging to use UTF-8 for its console handler.
        try {
            java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
            for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof java.util.logging.ConsoleHandler) {
                    handler.setEncoding("UTF-8");
                }
            }
        } catch (Exception e) {
            // Silently ignore if the logging configuration cannot be modified.
        }
        
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        System.out.println("Use 'rmi analyze <owner/repo>' to analyze a repository.");
        System.out.println("Use 'rmi --help' for more information.");
    }
}
