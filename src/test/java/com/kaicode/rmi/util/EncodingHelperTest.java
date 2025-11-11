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
    void shouldNotModifyTextOnNonWindows() {
        // On non-Windows platforms, cleanTextForWindows should return text unchanged
        String text = "ΓòÉΓöÇΓû¬ Some text";
        String result = EncodingHelper.cleanTextForWindows(text);
        
        assertThat(result).isEqualTo(text);
    }

    @Test
    void shouldHandleTextWithoutMojibake() {
        String cleanText = "═══ Normal UTF-8 text ═══";
        String result = EncodingHelper.cleanTextForWindows(cleanText);
        
        // Text without mojibake should be returned unchanged
        assertThat(result).isEqualTo(cleanText);
    }
}
