package com.kaicode.rmi.integration;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.llm.LLMAnalyzer;
import com.kaicode.rmi.llm.LLMClient;
import com.kaicode.rmi.model.LLMAnalysis;
import com.kaicode.rmi.util.EncodingHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration test for end-to-end Unicode display.
 * 
 * This test verifies that:
 * 1. UTF-8 configuration is properly set up
 * 2. Text cleaning removes mojibake artifacts
 * 3. Final output contains clean Unicode characters
 * 4. No mojibake appears in the complete analysis flow
 */
class UnicodeDisplayTest {

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        // Set up UTF-8 console streams
        EncodingHelper.setupUTF8ConsoleStreams();
        
        // Capture System.out for verification
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream, true, StandardCharsets.UTF_8));
    }

    @Test
    void shouldCleanBoxDrawingMojibakeInAnalysis() throws Exception {
        // Given: Mock LLM client that returns mojibake with box-drawing characters
        LLMClient mockClient = Mockito.mock(LLMClient.class);
        String mojibakeResponse = """
                {
                  "clarity": 7,
                  "completeness": 5,
                  "newcomerFriendly": 6,
                  "strengths": ["Well-structured with ΓöÇΓöÇΓöÇ borders"],
                  "suggestions": ["Add quick start section"]
                }
                """;
        
        when(mockClient.analyze(anyString()))
                .thenReturn(new LLMClient.LLMResponse(mojibakeResponse, 100));
        
        // When: Run analysis
        LLMAnalyzer analyzer = new LLMAnalyzer(mockClient);
        GitHubClient mockGitHub = Mockito.mock(GitHubClient.class);
        when(mockGitHub.getRecentCommits(anyString(), anyString(), Mockito.anyInt()))
                .thenReturn(java.util.List.of());
        
        LLMAnalysis analysis = analyzer.analyze(mockGitHub, "owner", "repo");
        
        // Then: Mojibake should be cleaned
        assertThat(analysis.getReadmeAnalysis().getStrengths())
                .noneMatch(s -> s.contains("ΓöÇ"))
                .anyMatch(s -> s.contains("───") || s.contains("-"));
    }

    @Test
    void shouldCleanDashMojibakeInCommitAnalysis() throws Exception {
        // Given: Mock client with dash mojibake
        LLMClient mockClient = Mockito.mock(LLMClient.class);
        String mojibakeJson = """
                {
                  "clarity": 8,
                  "consistency": 6,
                  "informativeness": 7,
                  "patterns": [
                    "Positive: Clear ΓÇô dashes used",
                    "Negative: Missing ΓÇæ hyphens"
                  ]
                }
                """;
        
        when(mockClient.analyze(anyString()))
                .thenReturn(new LLMClient.LLMResponse(mojibakeJson, 150));
        
        // When: Analyze commits with some commit data
        LLMAnalyzer analyzer = new LLMAnalyzer(mockClient);
        GitHubClient mockGitHub = Mockito.mock(GitHubClient.class);
        
        // Create mock commits so LLM is actually called
        com.kaicode.rmi.model.CommitInfo commit = com.kaicode.rmi.model.CommitInfo.builder()
                .sha("abc123")
                .message("Test commit")
                .author("author")
                .date(java.time.LocalDateTime.now())
                .build();
        when(mockGitHub.getRecentCommits(anyString(), anyString(), Mockito.anyInt()))
                .thenReturn(java.util.List.of(commit));
        
        LLMAnalysis analysis = analyzer.analyze(mockGitHub, "owner", "repo");
        
        // Then: Dash mojibake should be cleaned
        assertThat(analysis.getCommitAnalysis().getPatterns())
                .noneMatch(p -> p.contains("ΓÇô"))
                .noneMatch(p -> p.contains("ΓÇæ"))
                .anyMatch(p -> p.contains("-"));
    }

    @Test
    void shouldHandleUTF8OutputWithoutMojibake() {
        // Given: Text with box-drawing characters
        String cleanText = "═══ Repository Report ═══\n" +
                          "─── Metrics ───\n" +
                          "▪ Documentation: 85/100\n" +
                          "▪ Commit Quality: 90/100";
        
        // When: Print to console
        System.out.println(cleanText);
        System.out.flush();
        
        // Then: Output should contain the same clean text
        String output = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(output).contains("═══");
        assertThat(output).contains("───");
        assertThat(output).contains("▪");
    }

    @Test
    void shouldVerifyEncodingHelperCleansText() {
        // Given: Text with various mojibake patterns
        String mojibake = "ΓòÉΓòÉ Header ΓöÇΓöÇ Text Γû¬ Bullet ΓÇô dash";
        
        // When: Clean the text
        String cleaned = EncodingHelper.cleanTextForWindows(mojibake);
        
        // Then: All mojibake should be replaced
        assertThat(cleaned).doesNotContain("ΓòÉ", "ΓöÇ", "Γû¬", "ΓÇô");
        assertThat(cleaned).contains("═", "─", "▪", "-");
    }

    @Test
    void shouldCleanMojibakeInCommunityAnalysis() throws Exception {
        // Given: Mock client with mojibake in community analysis
        LLMClient mockClient = Mockito.mock(LLMClient.class);
        
        // Mock README analysis response
        when(mockClient.analyze(Mockito.contains("README")))
                .thenReturn(new LLMClient.LLMResponse("""
                        {
                          "clarity": 5,
                          "completeness": 4,
                          "newcomerFriendly": 5,
                          "strengths": ["Has basic structure"],
                          "suggestions": ["Add getting started guide"]
                        }
                        """, 100));
        
        // Mock community analysis response
        when(mockClient.analyze(Mockito.contains("community")))
                .thenReturn(new LLMClient.LLMResponse("""
                        {
                          "responsiveness": 3,
                          "helpfulness": 3,
                          "tone": 4,
                          "strengths": ["Active community"],
                          "suggestions": ["Improve response time ΓÇô be faster"]
                        }
                        """, 100));
        
        // When: Run full analysis
        LLMAnalyzer analyzer = new LLMAnalyzer(mockClient);
        GitHubClient mockGitHub = Mockito.mock(GitHubClient.class);
        when(mockGitHub.getRecentCommits(anyString(), anyString(), Mockito.anyInt()))
                .thenReturn(java.util.List.of());
        
        LLMAnalysis analysis = analyzer.analyze(mockGitHub, "owner", "repo");
        
        // Then: Mojibake should be cleaned
        assertThat(analysis.getCommunityAnalysis().getSuggestions())
                .noneMatch(s -> s.contains("ΓÇô"))
                .anyMatch(s -> s.contains("-"));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);
    }
}
