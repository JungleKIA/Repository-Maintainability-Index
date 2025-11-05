package com.kaicode.rmi.cli;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzeCommandIntegrationTest {

    @Test
    void shouldSetRepositoryField() throws Exception {
        AnalyzeCommand command = new AnalyzeCommand();
        
        Field repoField = AnalyzeCommand.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(command, "owner/repo");
        
        Field tokenField = AnalyzeCommand.class.getDeclaredField("token");
        tokenField.setAccessible(true);
        assertThat(tokenField.get(command)).isNull();
    }

    @Test
    void shouldSetTokenField() throws Exception {
        AnalyzeCommand command = new AnalyzeCommand();
        
        Field tokenField = AnalyzeCommand.class.getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(command, "test-token");
        
        assertThat(tokenField.get(command)).isEqualTo("test-token");
    }

    @Test
    void shouldSetFormatField() throws Exception {
        AnalyzeCommand command = new AnalyzeCommand();
        
        Field formatField = AnalyzeCommand.class.getDeclaredField("format");
        formatField.setAccessible(true);
        formatField.set(command, "json");
        
        assertThat(formatField.get(command)).isEqualTo("json");
    }

    @Test
    void shouldHaveDefaultFormat() throws Exception {
        AnalyzeCommand command = new AnalyzeCommand();
        
        Field formatField = AnalyzeCommand.class.getDeclaredField("format");
        formatField.setAccessible(true);
        
        assertThat(formatField.get(command)).isEqualTo("text");
    }
}
