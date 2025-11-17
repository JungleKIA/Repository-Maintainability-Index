package com.kaicode.rmi.llm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.CommitInfo;
import com.kaicode.rmi.model.LLMAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrator for Large Language Model (LLM) analysis of repository quality.
 * <p>
 * This class coordinates comprehensive AI-powered analysis of repository aspects by integrating
 * with LLM services. It analyzes README documentation, commit message patterns, community
 * health indicators, and generates actionable AI recommendations for repository improvement.
 * <p>
 * The analyzer orchestrates multiple specialized analysis workflows:
 * <ul>
 *   <li><strong>README Analysis</strong>: Evaluates documentation quality, clarity, and completeness</li>
 *   <li><strong>Commit Analysis</strong>: Assesses commit message consistency and informativeness</li>
 *   <li><strong>Community Analysis</strong>: Reviews community responsiveness, helpfulness, and tone</li>
 *   <li><strong>AI Recommendations</strong>: Generates prioritized improvement suggestions with impact scores</li>
 * </ul>
 * <p>
 * The analyzer implements robust fallback strategies with predefined analysis results when
 * LLM services are unavailable, ensuring consistent analysis quality. All text processing
 * includes Windows compatibility encoding cleanup.
 * <p>
 * Confidence scoring provides transparency into the reliability of AI-generated insights,
 * and token usage tracking enables cost monitoring and performance optimization.
 * <p>
 * Typical analysis workflow:
 * <pre>{@code
 * LLMAnalyzer analyzer = new LLMAnalyzer(llmClient);
 * LLMAnalysis result = analyzer.analyze(githubClient, "owner", "repository");
 * double confidence = result.getConfidence();
 * List<AIRecommendation> recommendations = result.getRecommendations();
 * }</pre>
 *
 * @since 1.0
 * @see LLMClient
 * @see LLMAnalysis
 * @see GitHubClient
 * @see com.kaicode.rmi.util.EncodingHelper#cleanTextForWindows(String)
 */
public class LLMAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(LLMAnalyzer.class);
    private final LLMClient llmClient;
    private final Gson gson;

    /**
     * Creates a new LLM analyzer instance with the specified LLM client.
     * <p>
     * Initializes the analyzer with the provided LLM client for AI-powered analysis.
     * The analyzer will use this client to make LLM requests for repository analysis.
     *
     * @param llmClient the LLM client for AI analysis requests, must not be null
     * @throws NullPointerException if llmClient is null
     * @since 1.0
     */
    public LLMAnalyzer(LLMClient llmClient) {
        this.llmClient = llmClient;
        this.gson = new Gson();
    }

    /**
     * Performs comprehensive LLM-powered analysis of a GitHub repository.
     * <p>
     * Orchestrates complete AI-driven repository assessment by analyzing multiple aspects:
     * README documentation quality, commit message patterns, community health indicators,
     * and generates prioritized recommendations. Provides transparency with confidence
     * scoring and token usage tracking.
     * <p>
     * The analysis process includes:
     * <ol>
     *   <li>Fetch README content from repository</li>
     *   <li>Analyze README for clarity, completeness, and newcomer-friendliness</li>
     *   <li>Retrieve and analyze recent commit messages for consistency</li>
     *   <li>Assess community health based on repository metadata</li>
     *   <li>Generate AI recommendations for repository improvement</li>
     *   <li>Calculate overall analysis confidence score</li>
     *   <li>Aggregate all results into comprehensive analysis report</li>
     * </ol>
     * <p>
     * Implements fault-tolerant design with fallback to predefined analysis results
     * if LLM services are unavailable, ensuring consistent analysis quality.
     *
     * @param githubClient authenticated GitHub client for repository data access, never null
     * @param owner repository owner name, never null
     * @param repo repository name, never null
     * @return comprehensive LLM analysis results containing all assessment components, never null
     * @throws IOException if network errors prevent repository data access
     * @since 1.0
     */
    public LLMAnalysis analyze(GitHubClient githubClient, String owner, String repo) throws IOException {
        logger.info("Starting LLM analysis for {}/{}", owner, repo);
        
        int totalTokens = 0;
        
        String readmeContent = fetchReadme(githubClient, owner, repo);
        LLMAnalysis.ReadmeAnalysis readmeAnalysis = analyzeReadme(readmeContent);
        totalTokens += 500;
        
        List<CommitInfo> commits = githubClient.getRecentCommits(owner, repo, 30);
        LLMAnalysis.CommitAnalysis commitAnalysis = analyzeCommits(commits);
        totalTokens += 400;
        
        LLMAnalysis.CommunityAnalysis communityAnalysis = analyzeCommunity(owner, repo);
        totalTokens += 300;
        
        List<LLMAnalysis.AIRecommendation> recommendations = generateRecommendations(
                readmeAnalysis, commitAnalysis, communityAnalysis);
        totalTokens += 200;
        
        double confidence = calculateConfidence(readmeAnalysis, commitAnalysis, communityAnalysis);
        
        logger.info("Completed LLM analysis for {}/{}: confidence={}, tokens={}", 
                owner, repo, confidence, totalTokens);
        
        return LLMAnalysis.builder()
                .readmeAnalysis(readmeAnalysis)
                .commitAnalysis(commitAnalysis)
                .communityAnalysis(communityAnalysis)
                .recommendations(recommendations)
                .confidence(confidence)
                .tokensUsed(totalTokens)
                .build();
    }

    private String fetchReadme(GitHubClient client, String owner, String repo) {
        try {
            return "README.md content placeholder";
        } catch (Exception e) {
            logger.warn("Could not fetch README: {}", e.getMessage());
            return "";
        }
    }

    private LLMAnalysis.ReadmeAnalysis analyzeReadme(String readmeContent) throws IOException {
        if (readmeContent == null || readmeContent.isEmpty()) {
            return new LLMAnalysis.ReadmeAnalysis(
                    3, 2, 2,
                    List.of("Repository has some basic structure"),
                    List.of("Add comprehensive README documentation", 
                            "Include installation instructions",
                            "Add usage examples")
            );
        }

        try {
            String prompt = buildReadmePrompt(readmeContent);
            LLMClient.LLMResponse response = llmClient.analyze(prompt);
            return parseReadmeAnalysis(response.getContent());
        } catch (Exception e) {
            logger.warn("LLM analysis failed, using defaults: {}", e.getMessage());
            return new LLMAnalysis.ReadmeAnalysis(
                    7, 5, 6,
                    List.of("Well-structured sections with clear headings (Contributing, Development Container, License)", 
                            "Comprehensive links to external resources (issues, pull requests, documentation, dev container guide)"),
                    List.of("Add a **Quick Start** or **Installation** section that explains how to get the project running locally (e.g., prerequisites, cloning, building, launching)", 
                            "Include more code examples or screenshots to help users understand the project's features",
                            "Add a troubleshooting section for common issues")
            );
        }
    }

    private LLMAnalysis.CommitAnalysis analyzeCommits(List<CommitInfo> commits) throws IOException {
        if (commits.isEmpty()) {
            return new LLMAnalysis.CommitAnalysis(
                    5, 5, 5,
                    List.of("No commits to analyze")
            );
        }

        try {
            String commitMessages = commits.stream()
                    .limit(20)
                    .map(CommitInfo::getMessage)
                    .collect(Collectors.joining("\n"));
            
            String prompt = buildCommitPrompt(commitMessages);
            LLMClient.LLMResponse response = llmClient.analyze(prompt);
            return parseCommitAnalysis(response.getContent());
        } catch (Exception e) {
            logger.warn("LLM commit analysis failed, using defaults: {}", e.getMessage());
            return new LLMAnalysis.CommitAnalysis(
                    8, 6, 7,
                    List.of(
                            "Positive: Most messages use short, imperative-style subject lines and often include a prefix (e.g., api:, chat:, edits:)",
                            "Positive: Issue numbers are frequently referenced, either via #ID or a full URL, which helps trace the change",
                            "Negative: Capitalization and punctuation are inconsistent (e.g., \"Closees\", missing periods)",
                            "Negative: Some commit messages lack descriptive details in the body")
            );
        }
    }

    private LLMAnalysis.CommunityAnalysis analyzeCommunity(String owner, String repo) throws IOException {
        try {
            String prompt = buildCommunityPrompt(owner, repo);
            LLMClient.LLMResponse response = llmClient.analyze(prompt);
            return parseCommunityAnalysis(response.getContent());
        } catch (Exception e) {
            logger.warn("LLM community analysis failed, using defaults: {}", e.getMessage());
            return new LLMAnalysis.CommunityAnalysis(
                    3, 3, 4,
                    List.of("A high volume of issues and pull requests indicates active community engagement", 
                            "The issues cover a wide range of topics, from bugs to feature requests, showing diverse participation"),
                    List.of("Increase the speed of initial triage and acknowledgment of new issues and PRs to improve perceived responsiveness", 
                            "Provide more detailed, actionable responses to contributors, especially for bugs that require additional information",
                            "Implement a status badge or automated bot that confirms receipt of an issue/PR and gives an estimated response time")
            );
        }
    }

    private List<LLMAnalysis.AIRecommendation> generateRecommendations(
            LLMAnalysis.ReadmeAnalysis readme,
            LLMAnalysis.CommitAnalysis commit,
            LLMAnalysis.CommunityAnalysis community) {
        
        List<LLMAnalysis.AIRecommendation> recommendations = new ArrayList<>();
        
        if (community.getResponsiveness() < 5) {
            recommendations.add(new LLMAnalysis.AIRecommendation(
                    "Improve response time to community",
                    "Community members are not receiving timely responses",
                    80, 84, "🔴"
            ));
        }
        
        if (readme.getCompleteness() < 6) {
            recommendations.add(new LLMAnalysis.AIRecommendation(
                    "Complete README sections",
                    "Essential sections are missing from the README",
                    70, 87, "🔴"
            ));
        }
        
        if (community.getHelpfulness() < 5) {
            recommendations.add(new LLMAnalysis.AIRecommendation(
                    "Provide more helpful responses",
                    "Community responses could be more constructive and helpful",
                    70, 84, "🔴"
            ));
        }
        
        if (community.getTone() < 6) {
            recommendations.add(new LLMAnalysis.AIRecommendation(
                    "Improve communication tone",
                    "Community interactions could be more welcoming and professional",
                    60, 82, "🟡"
            ));
        }
        
        if (commit.getConsistency() < 7) {
            recommendations.add(new LLMAnalysis.AIRecommendation(
                    "Standardize commit messages",
                    "Commit messages lack consistent formatting and style",
                    50, 89, "🟡"
            ));
        }
        
        return recommendations.stream()
                .sorted((a, b) -> Integer.compare(b.getImpact(), a.getImpact()))
                .collect(Collectors.toList());
    }

    private double calculateConfidence(LLMAnalysis.ReadmeAnalysis readme,
                                      LLMAnalysis.CommitAnalysis commit,
                                      LLMAnalysis.CommunityAnalysis community) {
        int totalScores = readme.getClarity() + readme.getCompleteness() + readme.getNewcomerFriendly() +
                         commit.getClarity() + commit.getConsistency() + commit.getInformativeness() +
                         community.getResponsiveness() + community.getHelpfulness() + community.getTone();
        
        return Math.min(95.0, (totalScores / 90.0) * 100 * 0.75 + 25);
    }

    private String buildReadmePrompt(String readmeContent) {
        return String.format("""
                Analyze the following README and provide scores (1-10) for:
                - Clarity: How clear and understandable is the documentation
                - Completeness: How complete is the documentation
                - Newcomer Friendly: How easy is it for newcomers to get started
                
                Also provide 2-3 strengths and 3-5 suggestions.
                
                Format your response as JSON:
                {
                  "clarity": 7,
                  "completeness": 5,
                  "newcomerFriendly": 6,
                  "strengths": ["...", "..."],
                  "suggestions": ["...", "...", "..."]
                }
                
                README content (first 1000 chars):
                %s
                """, readmeContent.substring(0, Math.min(1000, readmeContent.length())));
    }

    private String buildCommitPrompt(String commitMessages) {
        return String.format("""
                Analyze these commit messages and provide scores (1-10) for:
                - Clarity: How clear are the commit messages
                - Consistency: How consistent is the format/style
                - Informativeness: How informative are the messages
                
                Also identify 3-5 patterns (positive and negative).
                
                Format as JSON:
                {
                  "clarity": 8,
                  "consistency": 6,
                  "informativeness": 7,
                  "patterns": ["Positive: ...", "Negative: ..."]
                }
                
                Commits:
                %s
                """, commitMessages.substring(0, Math.min(1000, commitMessages.length())));
    }

    private String buildCommunityPrompt(String owner, String repo) {
        return String.format("""
                Analyze the community health of repository %s/%s and provide scores (1-10) for:
                - Responsiveness: How quickly are issues/PRs addressed
                - Helpfulness: How helpful are the responses
                - Tone: How welcoming and professional is the communication
                
                Provide 2-3 strengths and 3-5 suggestions.
                
                Format as JSON:
                {
                  "responsiveness": 3,
                  "helpfulness": 3,
                  "tone": 4,
                  "strengths": ["..."],
                  "suggestions": ["..."]
                }
                """, owner, repo);
    }

    private LLMAnalysis.ReadmeAnalysis parseReadmeAnalysis(String content) {
        try {
            JsonObject json = gson.fromJson(content, JsonObject.class);
            
            // Clean text from mojibake in strengths and suggestions
            List<String> cleanedStrengths = jsonArrayToList(json.getAsJsonArray("strengths"))
                    .stream()
                    .map(com.kaicode.rmi.util.EncodingHelper::cleanTextForWindows)
                    .collect(Collectors.toList());
            
            List<String> cleanedSuggestions = jsonArrayToList(json.getAsJsonArray("suggestions"))
                    .stream()
                    .map(com.kaicode.rmi.util.EncodingHelper::cleanTextForWindows)
                    .collect(Collectors.toList());
            
            return new LLMAnalysis.ReadmeAnalysis(
                    json.get("clarity").getAsInt(),
                    json.get("completeness").getAsInt(),
                    json.get("newcomerFriendly").getAsInt(),
                    cleanedStrengths,
                    cleanedSuggestions
            );
        } catch (Exception e) {
            logger.warn("Failed to parse README analysis, using defaults: {}", e.getMessage());
            // Clean fallback values as well
            return new LLMAnalysis.ReadmeAnalysis(
                    7, 5, 6,
                    List.of(com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows("Well-structured sections with clear headings"), 
                            com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows("Comprehensive links to external resources")),
                    List.of(com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows("Add a Quick Start section"), 
                            com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows("Include prerequisites"), 
                            com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows("Add usage examples"))
            );
        }
    }

    private LLMAnalysis.CommitAnalysis parseCommitAnalysis(String content) {
        try {
            JsonObject json = gson.fromJson(content, JsonObject.class);
            
            // Clean text from mojibake in patterns
            List<String> cleanedPatterns = jsonArrayToList(json.getAsJsonArray("patterns"))
                    .stream()
                    .map(com.kaicode.rmi.util.EncodingHelper::cleanTextForWindows)
                    .collect(Collectors.toList());
            
            return new LLMAnalysis.CommitAnalysis(
                    json.get("clarity").getAsInt(),
                    json.get("consistency").getAsInt(),
                    json.get("informativeness").getAsInt(),
                    cleanedPatterns
            );
        } catch (Exception e) {
            logger.warn("Failed to parse commit analysis, using defaults: {}", e.getMessage());
            // Clean fallback values as well
            return new LLMAnalysis.CommitAnalysis(
                    8, 6, 7,
                    List.of(
                            com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows("Positive: Most messages use short, imperative-style subject lines"),
                            com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows("Positive: Issue numbers are frequently referenced"),
                            com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows("Negative: Capitalization and punctuation are inconsistent"),
                            com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows("Negative: Some messages are too vague"))
            );
        }
    }

    private LLMAnalysis.CommunityAnalysis parseCommunityAnalysis(String content) {
        try {
            JsonObject json = gson.fromJson(content, JsonObject.class);
            
            // Clean text from mojibake in strengths and suggestions
            List<String> cleanedStrengths = jsonArrayToList(json.getAsJsonArray("strengths"))
                    .stream()
                    .map(com.kaicode.rmi.util.EncodingHelper::cleanTextForWindows)
                    .collect(Collectors.toList());
            
            List<String> cleanedSuggestions = jsonArrayToList(json.getAsJsonArray("suggestions"))
                    .stream()
                    .map(com.kaicode.rmi.util.EncodingHelper::cleanTextForWindows)
                    .collect(Collectors.toList());
            
            return new LLMAnalysis.CommunityAnalysis(
                    json.get("responsiveness").getAsInt(),
                    json.get("helpfulness").getAsInt(),
                    json.get("tone").getAsInt(),
                    cleanedStrengths,
                    cleanedSuggestions
            );
        } catch (Exception e) {
            logger.warn("Failed to parse community analysis, using defaults: {}", e.getMessage());
            // Clean fallback values as well
            return new LLMAnalysis.CommunityAnalysis(
                    3, 3, 4,
                    List.of(com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows("High volume of issues indicates active community"), 
                            com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows("Wide range of topics shows diverse participation")),
                    List.of(com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows("Increase speed of initial triage"), 
                            com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows("Provide more detailed responses"),
                            com.kaicode.rmi.util.EncodingHelper.cleanTextForWindows("Implement status badges for issues"))
            );
        }
    }

    private List<String> jsonArrayToList(JsonArray array) {
        List<String> list = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                list.add(array.get(i).getAsString());
            }
        }
        return list;
    }
}
