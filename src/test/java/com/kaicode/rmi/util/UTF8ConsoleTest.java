package com.kaicode.rmi.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

class UTF8ConsoleTest {

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void shouldInitializeWithoutException() {
        UTF8Console.initialize();
        
        // Should not throw exception
        assertThat(true).isTrue();
    }

    @Test
    void shouldPrintText() {
        UTF8Console.initialize();
        UTF8Console.print("Test");
        UTF8Console.flush();
        
        String output = outContent.toString();
        assertThat(output).contains("Test");
    }

    @Test
    void shouldPrintlnText() {
        UTF8Console.initialize();
        UTF8Console.println("Test line");
        UTF8Console.flush();
        
        String output = outContent.toString();
        assertThat(output).contains("Test line");
    }

    @Test
    void shouldPrintEmptyLine() {
        UTF8Console.initialize();
        UTF8Console.println();
        UTF8Console.flush();
        
        String output = outContent.toString();
        assertThat(output).isNotNull();
    }

    @Test
    void shouldPrintUnicodeCharacters() {
        UTF8Console.initialize();
        UTF8Console.println("═══ ─── ▪");
        UTF8Console.flush();
        
        String output = outContent.toString();
        assertThat(output).contains("═");
        assertThat(output).contains("─");
        assertThat(output).contains("▪");
    }

    @Test
    void shouldPrintErrorText() {
        UTF8Console.initialize();
        UTF8Console.printErr("Error text");
        UTF8Console.flush();
        
        String output = errContent.toString();
        assertThat(output).contains("Error text");
    }

    @Test
    void shouldPrintlnErrorText() {
        UTF8Console.initialize();
        UTF8Console.printlnErr("Error line");
        UTF8Console.flush();
        
        String output = errContent.toString();
        assertThat(output).contains("Error line");
    }

    @Test
    void shouldWorkWithoutInitialization() {
        // Should fallback to System.out if not initialized
        // Note: UTF8Console uses static fields, so we can't really test uninitialized state
        // after it's been initialized by other tests. This test just verifies no exception is thrown.
        UTF8Console.print("Fallback test");
        UTF8Console.flush();
        
        // No exception thrown is success
        assertThat(true).isTrue();
    }

    @Test
    void shouldFlushWithoutErrors() {
        UTF8Console.initialize();
        UTF8Console.flush();
        
        // Should not throw exception
        assertThat(true).isTrue();
    }

    @Test
    void shouldHandleMultipleCalls() {
        UTF8Console.initialize();
        UTF8Console.println("Line 1");
        UTF8Console.println("Line 2");
        UTF8Console.println("Line 3");
        UTF8Console.flush();
        
        String output = outContent.toString();
        assertThat(output).contains("Line 1");
        assertThat(output).contains("Line 2");
        assertThat(output).contains("Line 3");
    }

    @Test
    void shouldHandleComplexUnicodeReport() {
        UTF8Console.initialize();
        
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════════\n");
        report.append("  Repository Maintainability Index Report\n");
        report.append("═══════════════════════════════════════════════════════════════\n");
        report.append("\n");
        report.append("───────────────────────────────────────────────────────────────\n");
        report.append("  Detailed Metrics\n");
        report.append("───────────────────────────────────────────────────────────────\n");
        report.append("\n");
        report.append("▪ Documentation: 80.00/100\n");
        report.append("▪ Commit Quality: 100.00/100\n");
        
        UTF8Console.println(report.toString());
        UTF8Console.flush();
        
        String output = outContent.toString();
        assertThat(output).contains("═");
        assertThat(output).contains("─");
        assertThat(output).contains("▪");
        assertThat(output).contains("Repository Maintainability Index Report");
        assertThat(output).contains("Detailed Metrics");
    }
}
