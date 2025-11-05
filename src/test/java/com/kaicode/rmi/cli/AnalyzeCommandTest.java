package com.kaicode.rmi.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzeCommandTest {

    @Test
    void shouldFailWithInvalidRepositoryFormat() {
        AnalyzeCommand command = new AnalyzeCommand();
        CommandLine cmd = new CommandLine(command);

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        int exitCode = cmd.execute("invalid-repo-format");

        assertThat(exitCode).isEqualTo(1);
    }

    @Test
    void shouldParseRepositoryCorrectly() {
        AnalyzeCommand command = new AnalyzeCommand();
        CommandLine cmd = new CommandLine(command);

        cmd.parseArgs("owner/repo");

        assertThat(cmd.getParseResult()).isNotNull();
    }

    @Test
    void shouldAcceptTokenOption() {
        AnalyzeCommand command = new AnalyzeCommand();
        CommandLine cmd = new CommandLine(command);

        cmd.parseArgs("owner/repo", "--token", "test-token");

        assertThat(cmd.getParseResult()).isNotNull();
    }

    @Test
    void shouldAcceptFormatOption() {
        AnalyzeCommand command = new AnalyzeCommand();
        CommandLine cmd = new CommandLine(command);

        cmd.parseArgs("owner/repo", "--format", "json");

        assertThat(cmd.getParseResult()).isNotNull();
    }

    @Test
    void shouldHaveHelpOption() {
        AnalyzeCommand command = new AnalyzeCommand();
        CommandLine cmd = new CommandLine(command);

        assertThat(cmd.getCommandSpec().optionsMap()).containsKey("-h");
        assertThat(cmd.getCommandSpec().optionsMap()).containsKey("--help");
    }
}
