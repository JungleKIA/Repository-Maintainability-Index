package com.kaicode.rmi;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class MainTest {

    @Test
    void shouldShowHelpWhenNoArgumentsProvided() {
        Main main = new Main();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        
        try {
            System.setOut(new PrintStream(outContent));
            main.run();
            
            String output = outContent.toString();
            assertThat(output).contains("analyze");
            assertThat(output).contains("help");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void shouldHaveVersionOption() {
        Main main = new Main();
        CommandLine cmd = new CommandLine(main);

        assertThat(cmd.getCommandSpec().optionsMap()).containsKey("--version");
    }

    @Test
    void shouldHaveAnalyzeSubcommand() {
        Main main = new Main();
        CommandLine cmd = new CommandLine(main);

        assertThat(cmd.getSubcommands()).containsKey("analyze");
    }
}
