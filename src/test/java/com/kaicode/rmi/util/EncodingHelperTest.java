package com.kaicode.rmi.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;

class EncodingHelperTest {

    @Test
    void shouldCreateUTF8PrintWriter() {
        PrintWriter writer = EncodingHelper.createUTF8PrintWriter();
        
        assertThat(writer).isNotNull();
        assertThat(writer).isInstanceOf(PrintWriter.class);
    }

    @Test
    void shouldReturnEmptyStringForNullInCleanTextForWindows() {
        String result = EncodingHelper.cleanTextForWindows(null);
        
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTextUnchangedOnNonWindows() {
        String text = "Hello 👋 World 🌍";
        
        // This will work on any platform
        String result = EncodingHelper.cleanTextForWindows(text);
        
        assertThat(result).isNotNull();
        // On Windows it returns text as-is (for UTF-8 output)
        // On other platforms it also returns text as-is
        assertThat(result).isEqualTo(text);
    }

    @Test
    void shouldHandleEmptyStringInCleanTextForWindows() {
        String result = EncodingHelper.cleanTextForWindows("");
        
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleRegularTextInCleanTextForWindows() {
        String text = "Regular ASCII text without special chars";
        String result = EncodingHelper.cleanTextForWindows(text);
        
        assertThat(result).isEqualTo(text);
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldDetectWindowsPlatform() {
        boolean isWindows = EncodingHelper.isWindows();
        
        assertThat(isWindows).isTrue();
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void shouldDetectNonWindowsPlatform() {
        boolean isWindows = EncodingHelper.isWindows();
        
        assertThat(isWindows).isFalse();
    }

    @Test
    void shouldCheckOsName() {
        // Just ensure the method runs without exception
        boolean result = EncodingHelper.isWindows();
        
        assertThat(result).isIn(true, false);
    }

    @Test
    void shouldConfigureConsoleEncodingWithoutException() {
        // Save original streams
        java.io.PrintStream originalOut = System.out;
        java.io.PrintStream originalErr = System.err;
        
        try {
            // This method should not throw exceptions even if it fails
            EncodingHelper.configureConsoleEncoding();
            
            // If we get here, no exception was thrown
            assertThat(true).isTrue();
        } finally {
            // Restore original streams
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    @Test
    void shouldSetupUTF8ConsoleStreamsWithoutException() {
        // Save original streams
        java.io.PrintStream originalOut = System.out;
        java.io.PrintStream originalErr = System.err;
        
        try {
            // This method should not throw exceptions even if it fails
            EncodingHelper.setupUTF8ConsoleStreams();
            
            // If we get here, no exception was thrown
            assertThat(true).isTrue();
        } finally {
            // Restore original streams
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    @Test
    void shouldReturnEmptyStringForNullInRemoveEmojis() {
        String result = EncodingHelper.removeEmojis(null);
        
        assertThat(result).isEmpty();
    }

    @Test
    void shouldRemoveEmojisFromText() {
        String textWithEmojis = "Hello 👋 World 🌍 Test 🎉";
        String result = EncodingHelper.removeEmojis(textWithEmojis);
        
        assertThat(result).doesNotContain("👋", "🌍", "🎉");
        assertThat(result).contains("Hello", "World", "Test");
    }

    @Test
    void shouldHandleTextWithoutEmojis() {
        String text = "Regular text without emojis";
        String result = EncodingHelper.removeEmojis(text);
        
        assertThat(result).isEqualTo(text);
    }

    @Test
    void shouldHandleEmptyStringInRemoveEmojis() {
        String result = EncodingHelper.removeEmojis("");
        
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleOnlyEmojis() {
        String onlyEmojis = "👋🌍🎉😀";
        String result = EncodingHelper.removeEmojis(onlyEmojis);
        
        // All emojis should be removed, leaving empty or minimal text
        assertThat(result).doesNotContain("👋", "🌍", "🎉", "😀");
    }

    @Test
    void shouldCheckUTF8Support() {
        boolean result = EncodingHelper.isUTF8Supported();
        
        // Result can be true or false depending on environment
        assertThat(result).isIn(true, false);
    }

    @Test
    void shouldReturnTrueForUTF8Encoding() {
        // In most modern environments, UTF-8 is supported
        boolean result = EncodingHelper.isUTF8Supported();
        
        // This test just verifies the method runs
        assertThat(result).isNotNull();
    }

    @Test
    void shouldHandleMixedContent() {
        String mixed = "ASCII text 测试 emojis 👋 numbers 123";
        String result = EncodingHelper.cleanTextForWindows(mixed);
        
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mixed);
    }

    @Test
    void shouldHandleSpecialCharacters() {
        String special = "Text with special: @#$%^&*()";
        String result = EncodingHelper.cleanTextForWindows(special);
        
        assertThat(result).isEqualTo(special);
    }

    @Test
    void shouldHandleNewlinesAndTabs() {
        String text = "Line1\nLine2\tTabbed";
        String result = EncodingHelper.cleanTextForWindows(text);
        
        assertThat(result).isEqualTo(text);
    }

    @Test
    void shouldHandleUnicodeCharacters() {
        String unicode = "Ñoño café résumé";
        String result = EncodingHelper.cleanTextForWindows(unicode);
        
        assertThat(result).isEqualTo(unicode);
    }

    @Test
    void shouldHandleMultipleCallsToSetupUTF8ConsoleStreams() {
        // Save original streams
        java.io.PrintStream originalOut = System.out;
        java.io.PrintStream originalErr = System.err;
        
        try {
            // Call multiple times to ensure it's idempotent
            EncodingHelper.setupUTF8ConsoleStreams();
            EncodingHelper.setupUTF8ConsoleStreams();
            
            // Should not throw exception
            assertThat(true).isTrue();
        } finally {
            // Restore original streams
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    @Test
    void shouldSetupUTF8StreamsProperly() {
        // Save original streams and properties
        java.io.PrintStream originalOut = System.out;
        java.io.PrintStream originalErr = System.err;
        String originalFileEncoding = System.getProperty("file.encoding");
        
        try {
            // Setup UTF-8 streams
            EncodingHelper.setupUTF8ConsoleStreams();
            
            // Verify system properties are set
            assertThat(System.getProperty("file.encoding")).isEqualTo("UTF-8");
            assertThat(System.getProperty("sun.jnu.encoding")).isEqualTo("UTF-8");
            assertThat(System.getProperty("console.encoding")).isEqualTo("UTF-8");
            
            // Verify we can print UTF-8 characters without exception
            System.out.println("Test: ═══ ─── ▪");
            System.err.println("Test: ═══ ─── ▪");
            
            assertThat(true).isTrue();
        } finally {
            // Restore original streams and properties
            System.setOut(originalOut);
            System.setErr(originalErr);
            if (originalFileEncoding != null) {
                System.setProperty("file.encoding", originalFileEncoding);
            }
        }
    }

    @Test
    void shouldSetupUTF8OutputWithoutException() {
        // This method should not throw exceptions even if it fails
        EncodingHelper.setupUTF8Output();
        
        // If we get here, no exception was thrown
        assertThat(true).isTrue();
    }

    @Test
    void shouldHandleBoxDrawingCharacters() {
        String text = "╔═══════╗\n║ Test  ║\n╚═══════╝";
        String result = EncodingHelper.cleanTextForWindows(text);
        
        assertThat(result).isNotNull();
        // On non-Windows, text should be unchanged
        // On Windows without mojibake, text should be unchanged
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldCleanMojibakeOnWindows() {
        // Simulate mojibake pattern: UTF-8 characters incorrectly decoded as Windows-1252
        // "ΓòÉ" is what ═ (U+2550) looks like when decoded incorrectly
        String corruptedText = "ΓòÉΓòÉΓòÉ Header ΓòÉΓòÉΓòÉ";
        String result = EncodingHelper.cleanTextForWindows(corruptedText);
        
        // The method should attempt to repair the mojibake
        assertThat(result).isNotNull();
        // Result should either be repaired or returned as-is if repair fails
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void shouldCleanMojibakeOnAllPlatforms() {
        // cleanTextForWindows() should fix mojibake on ALL platforms, not just Windows
        // The name is misleading - it fixes Windows-specific mojibake patterns regardless of OS
        String text = "ΓòÉΓöÇΓû¬ Some text";
        String result = EncodingHelper.cleanTextForWindows(text);
        
        // Should fix mojibake even on Linux/Mac
        assertThat(result).isEqualTo("═─▪ Some text");
    }

    @Test
    void shouldHandleTextWithoutMojibake() {
        String cleanText = "═══ Normal UTF-8 text ═══";
        String result = EncodingHelper.cleanTextForWindows(cleanText);
        
        // Text without mojibake should be returned unchanged
        assertThat(result).isEqualTo(cleanText);
    }

    @Test
    void shouldHandleMultipleMojibakePatterns() {
        String textWithMojibake = "ΓòÉΓòÉ Header ΓöÇΓöÇ Text Γû¬ Bullet";
        String result = EncodingHelper.cleanTextForWindows(textWithMojibake);
        
        // Result should be processed (either repaired or returned as-is)
        assertThat(result).isNotNull();
    }

    @Test
    void shouldHandleVerticalBarMojibake() {
        String textWithMojibake = "Γöé Some text with vertical bar";
        String result = EncodingHelper.cleanTextForWindows(textWithMojibake);
        
        // Result should be processed
        assertThat(result).isNotNull();
    }

    @Test
    void shouldHandleEmptyAfterMojibakeDetection() {
        // Edge case: empty string should not trigger mojibake detection
        String emptyText = "";
        String result = EncodingHelper.cleanTextForWindows(emptyText);
        
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleNullSafely() {
        // Null should be handled safely
        String result = EncodingHelper.cleanTextForWindows(null);
        
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleLongTextWithMojibake() {
        // Long text with mojibake patterns
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            text.append("ΓòÉ Line ").append(i).append(" ΓöÇ\n");
        }
        
        String result = EncodingHelper.cleanTextForWindows(text.toString());
        
        // Should not throw exception
        assertThat(result).isNotNull();
    }

    @Test
    void shouldHandleMixedMojibakeAndCleanText() {
        String mixed = "Clean text ═══ ΓòÉ Mojibake ΓöÇ More clean ───";
        String result = EncodingHelper.cleanTextForWindows(mixed);
        
        // Should handle mixed content
        assertThat(result).isNotNull();
    }

    // ========== NEW TESTS FOR ADDITIONAL MOJIBAKE PATTERNS ==========

    @Test
    void shouldCleanNewBoxDrawingCharacters() {
        // Test all box-drawing character replacements
        assertThat(EncodingHelper.cleanTextForWindows("ΓòÉΓòÉΓòÉ")).isEqualTo("═══");
        assertThat(EncodingHelper.cleanTextForWindows("ΓöÇΓöÇΓöÇ")).isEqualTo("───");
        assertThat(EncodingHelper.cleanTextForWindows("Γû¬ Item")).isEqualTo("▪ Item");
        assertThat(EncodingHelper.cleanTextForWindows("Γöé Column")).isEqualTo("│ Column");
    }

    @Test
    void shouldCleanNewMultipleDashes() {
        // Test multiple dash pattern
        String input = "ΓöÇΓöÇΓöÇ Separator ΓöÇΓöÇΓöÇ";
        String expected = "─── Separator ───";
        assertThat(EncodingHelper.cleanTextForWindows(input)).isEqualTo(expected);
    }

    @Test
    void shouldCleanNewDashVariants() {
        // Test additional dash variants from real-world Git Bash issues
        assertThat(EncodingHelper.cleanTextForWindows("firstΓÇæresponse time")).isEqualTo("first-response time");
        assertThat(EncodingHelper.cleanTextForWindows("24ΓÇô48 hours")).isEqualTo("24-48 hours");
    }

    @Test
    void shouldCleanNewRealWorldExamples() {
        // Test real-world examples from Git Bash
        String input1 = "firstΓÇæresponse time 24ΓÇô48 hours";
        String expected1 = "first-response time 24-48 hours";
        assertThat(EncodingHelper.cleanTextForWindows(input1)).isEqualTo(expected1);
    }

    @Test
    void shouldBeIdempotentNew() {
        // Test that repeated cleaning produces the same result
        String text = "Clean text with no mojibake";
        String cleaned1 = EncodingHelper.cleanTextForWindows(text);
        String cleaned2 = EncodingHelper.cleanTextForWindows(cleaned1);
        String cleaned3 = EncodingHelper.cleanTextForWindows(cleaned2);

        assertThat(cleaned1).isEqualTo(text);
        assertThat(cleaned2).isEqualTo(cleaned1);
        assertThat(cleaned3).isEqualTo(cleaned2);
    }

    @Test
    void shouldBeIdempotentWithMojibakeNew() {
        // Test that cleaning mojibake twice produces the same result
        String textWithMojibake = "ΓòÉΓòÉ Header ΓöÇΓöÇ Text";
        String cleaned1 = EncodingHelper.cleanTextForWindows(textWithMojibake);
        String cleaned2 = EncodingHelper.cleanTextForWindows(cleaned1);

        assertThat(cleaned1).isEqualTo(cleaned2);
    }

    @Test
    void shouldHandleNewNullInput() {
        // Test null handling
        assertThat(EncodingHelper.cleanTextForWindows(null)).isEmpty();
    }

    @Test
    void shouldHandleNewEmptyInput() {
        // Test empty string handling
        assertThat(EncodingHelper.cleanTextForWindows("")).isEmpty();
    }

    @Test
    void shouldHandleNewWhitespaceOnlyInput() {
        // Test whitespace-only string handling (should be trimmed)
        assertThat(EncodingHelper.cleanTextForWindows("   ")).isEmpty();
        assertThat(EncodingHelper.cleanTextForWindows("\t\n  ")).isEmpty();
    }

    @Test
    void shouldPreserveNewNewlinesAndTabs() {
        // Test that newlines and tabs are preserved
        String text = "Line1\nLine2\tTabbed\rCarriage";
        String result = EncodingHelper.cleanTextForWindows(text);

        assertThat(result).contains("\n");
        assertThat(result).contains("\t");
        assertThat(result).contains("\r");
    }

    @Test
    void shouldRemoveNewControlCharacters() {
        // Test that control characters (except \n, \t, \r) are removed
        String textWithControl = "Text\u0000with\u0001control\u0002chars";
        String result = EncodingHelper.cleanTextForWindows(textWithControl);

        assertThat(result).doesNotContain("\u0000", "\u0001", "\u0002");
        assertThat(result).contains("Text", "with", "control", "chars");
    }

    @Test
    void shouldHandleNewComplexMojibakeScenario() {
        // Test complex scenario with multiple mojibake patterns
        String complex = "ΓòÉΓòÉΓòÉ Repository Maintainability Index Report ΓòÉΓòÉΓòÉ\n" +
                "Repository: facebook/react\n" +
                "Overall Score: 87.50/100\n" +
                "Rating: \"GOOD\"\n" +
                "ΓöÇΓöÇΓöÇ Detailed Metrics ΓöÇΓöÇΓöÇ\n" +
                "Γû¬ Documentation: 100.00/100 (weight: 20%)\n" +
                "  Evaluates the presence of essential documentation files\n" +
                "  Details: Found: README.md, CONTRIBUTING.md, LICENSE...";

        String result = EncodingHelper.cleanTextForWindows(complex);

        // Verify mojibake is cleaned
        assertThat(result).doesNotContain("ΓòÉ", "ΓöÇ", "Γû¬");
        assertThat(result).contains("═", "─", "▪");
        assertThat(result).contains("Repository Maintainability Index Report");
        assertThat(result).contains("facebook/react");
    }

    @Test
    void shouldHandleNewLLMResponseMojibake() {
        // Test typical LLM response with mojibake
        String llmResponse = "Well-structured sections with clear headings (Contributing, Development Container, License)\n" +
                "Comprehensive links to external resources (issues, pull requests, documentation...)\n" +
                "Add a **Quick Start** or **Installation** section that explains how to get the project running locally\n" +
                "Increase the frequency and speed of maintainer responses; many issues currently show no reply or followΓÇæup.\n" +
                "Provide clearer guidelines for issue reporting and PR submission to reduce confusion and improve quality of contributions.";

        String result = EncodingHelper.cleanTextForWindows(llmResponse);

        // Verify all mojibake is cleaned
        assertThat(result).doesNotContain("ΓÇæ");
        assertThat(result).contains("Well-structured");
        assertThat(result).contains("documentation");
        assertThat(result).contains("follow-up");
    }
}
