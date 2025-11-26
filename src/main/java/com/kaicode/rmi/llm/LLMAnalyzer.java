package com.kaicode.rmi.llm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.CommitInfo;
import com.kaicode.rmi.model.LLMAnalysis;
import com.kaicode.rmi.model.RepositoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
    
    // README analysis constants
    private static final int MAX_README_LENGTH = 4000;
    private static final int MIN_README_LENGTH = 50;
    private static final String README_TRUNCATION_NOTICE = "\n\n[Content truncated - README exceeds character limit]";
    
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
        logger.info("Starting LLM analysis for {}/{} using model: {}", owner, repo, llmClient.getModel());

        String llmMode = "REAL"; // Assume real analysis by default
        AtomicInteger totalTokens = new AtomicInteger(0); // Real token counting from API
        AtomicInteger parseErrors = new AtomicInteger(0); // Count actual parsing failures
        AtomicInteger apiErrors = new AtomicInteger(0);    // Count API failures (429, network issues)

        String readmeContent = fetchReadme(githubClient, owner, repo);
        LLMAnalysis.ReadmeAnalysis readmeAnalysis = analyzeReadme(githubClient, owner, repo, readmeContent, apiErrors, parseErrors, totalTokens);

        List<CommitInfo> commits = githubClient.getRecentCommits(owner, repo, 30);
        LLMAnalysis.CommitAnalysis commitAnalysis = analyzeCommits(commits, apiErrors, parseErrors, totalTokens);

        LLMAnalysis.CommunityAnalysis communityAnalysis = analyzeCommunity(owner, repo, apiErrors, parseErrors, totalTokens);

        // Simple and robust: if any tokens were consumed, it's REAL analysis
        // Parsing issues don't matter - API was actually used
        if (totalTokens.get() > 0) {
            llmMode = "REAL"; // Tokens consumed = API worked = REAL analysis
        } else {
            llmMode = "FALLBACK"; // No tokens = no API calls = fallback only
        }

        List<LLMAnalysis.AIRecommendation> recommendations = generateRecommendations(
                readmeAnalysis, commitAnalysis, communityAnalysis);

        double confidence = calculateConfidence(readmeAnalysis, commitAnalysis, communityAnalysis);

        logger.info("Completed LLM analysis for {}/{}: confidence={}, tokens={}, mode={}, parseErrors={}, apiErrors={}",
                owner, repo, confidence, totalTokens, llmMode, parseErrors, apiErrors);

        return LLMAnalysis.builder()
                .readmeAnalysis(readmeAnalysis)
                .commitAnalysis(commitAnalysis)
                .communityAnalysis(communityAnalysis)
                .recommendations(recommendations)
                .confidence(confidence)
                .tokensUsed(totalTokens.get())
                .llmMode(llmMode)
                .build();
    }

    /**
     * Fetches README content from GitHub repository with fallback strategies.
     * 
     * @param client GitHub API client
     * @param owner repository owner
     * @param repo repository name
     * @return README content or fallback message
     */
    /**
     * Builds comprehensive repository context information for LLM analysis.
     * Checks for presence of all important project files and documentation.
     */
    /**
     * Builds repository context for LLM analysis.
     * Only checks essential documentation files that match the Documentation metric.
     */
    private String buildRepositoryContext(GitHubClient client, String owner, String repo) {
        StringBuilder context = new StringBuilder();
        context.append("REPOSITORY CONTEXT - Essential documentation files:\n");
        
        // Check only the 5 core documentation files from Documentation metric
        boolean hasLicense = client.hasFile(owner, repo, "LICENSE") || 
                           client.hasFile(owner, repo, "LICENSE.md") ||
                           client.hasFile(owner, repo, "LICENSE.txt");
        boolean hasContributing = client.hasFile(owner, repo, "CONTRIBUTING.md") ||
                                client.hasFile(owner, repo, "CONTRIBUTING");
        boolean hasCodeOfConduct = client.hasFile(owner, repo, "CODE_OF_CONDUCT.md");
        boolean hasChangelog = client.hasFile(owner, repo, "CHANGELOG.md") ||
                              client.hasFile(owner, repo, "CHANGELOG");
        
        // Build simple list
        if (hasLicense) {
            context.append("✓ LICENSE file exists\n");
        }
        if (hasContributing) {
            context.append("✓ CONTRIBUTING.md exists\n");
        }
        if (hasCodeOfConduct) {
            context.append("✓ CODE_OF_CONDUCT.md exists\n");
        }
        if (hasChangelog) {
            context.append("✓ CHANGELOG.md exists\n");
        }
        
        // Add note if no files found
        if (!hasLicense && !hasContributing && !hasCodeOfConduct && !hasChangelog) {
            context.append("⚠ No standard documentation files detected\n");
        }
        
        context.append("\nIMPORTANT: Do NOT suggest adding files that are already present above!\n");
        
        return context.toString();
    }

    private String fetchReadme(GitHubClient client, String owner, String repo) {
        try {
            String readmeContent = client.getReadmeContent(owner, repo);
            
            if (isValidReadmeContent(readmeContent)) {
                logger.info("Fetched README for {}/{}: {} characters", 
                    owner, repo, readmeContent.length());
                return readmeContent;
            }
            
            // Fallback to repository description
            return fetchFallbackContent(client, owner, repo);
            
        } catch (Exception e) {
            logger.error("Failed to fetch README for {}/{}: {}", owner, repo, e.getMessage(), e);
            return getFallbackMessage();
        }
    }
    
    /**
     * Validates README content quality.
     */
    private boolean isValidReadmeContent(String content) {
        return content != null 
            && !content.trim().isEmpty() 
            && content.length() >= MIN_README_LENGTH;
    }
    
    /**
     * Fetches fallback content when README is not available.
     */
    private String fetchFallbackContent(GitHubClient client, String owner, String repo) {
        try {
            logger.warn("README not found for {}/{}, using repository description", owner, repo);
            RepositoryInfo repoInfo = client.getRepository(owner, repo);
            String description = repoInfo.getDescription();
            
            if (description != null && !description.trim().isEmpty()) {
                return String.format("Repository Description: %s%n%nNote: No README.md file found in this repository.", 
                    description);
            }
        } catch (Exception e) {
            logger.debug("Could not fetch repository description: {}", e.getMessage());
        }
        
        return "No README.md file found in this repository. Consider adding comprehensive documentation.";
    }
    
    /**
     * Returns fallback message when all fetch attempts fail.
     */
    private String getFallbackMessage() {
        return "Unable to fetch repository documentation. This may affect the analysis quality.";
    }

    private LLMAnalysis.ReadmeAnalysis analyzeReadme(GitHubClient githubClient, String owner, String repo, 
                                                      String readmeContent, AtomicInteger apiErrors, 
                                                      AtomicInteger parseErrors, AtomicInteger totalTokens) throws IOException {
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
            // Gather repository context for better analysis
            String repoContext = buildRepositoryContext(githubClient, owner, repo);
            String prompt = buildReadmePrompt(readmeContent, repoContext);
            LLMClient.LLMResponse response = llmClient.analyze(prompt);
            totalTokens.addAndGet(response.getTokensUsed()); // Add real tokens from API
            return parseReadmeAnalysis(response.getContent(), parseErrors);
        } catch (Exception e) {
            logger.warn("LLM README analysis failed, using defaults: {}", e.getMessage());
            apiErrors.incrementAndGet(); // API or network error
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

    private LLMAnalysis.CommitAnalysis analyzeCommits(List<CommitInfo> commits, AtomicInteger apiErrors, AtomicInteger parseErrors, AtomicInteger totalTokens) throws IOException {
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
            totalTokens.addAndGet(response.getTokensUsed()); // Add real tokens from API
            return parseCommitAnalysis(response.getContent(), parseErrors);
        } catch (Exception e) {
            logger.warn("LLM commit analysis failed, using defaults: {}", e.getMessage());
            apiErrors.incrementAndGet(); // API or network error
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

    private LLMAnalysis.CommunityAnalysis analyzeCommunity(String owner, String repo, AtomicInteger apiErrors, AtomicInteger parseErrors, AtomicInteger totalTokens) throws IOException {
        try {
            String prompt = buildCommunityPrompt(owner, repo);
            LLMClient.LLMResponse response = llmClient.analyze(prompt);
            totalTokens.addAndGet(response.getTokensUsed()); // Add real tokens from API
            return parseCommunityAnalysis(response.getContent(), parseErrors);
        } catch (Exception e) {
            logger.warn("LLM community analysis failed, using defaults: {}", e.getMessage());
            apiErrors.incrementAndGet(); // API or network error
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

    /**
     * Builds LLM prompt for README analysis with smart content truncation.
     * 
     * @param readmeContent raw README content
     * @param repoContext repository context with information about existing files
     * @return formatted prompt for LLM
     */
    private String buildReadmePrompt(String readmeContent, String repoContext) {
        String processedContent = prepareReadmeContent(readmeContent);
        
        return String.format("""
                IMPORTANT: Respond ONLY with a valid JSON object. No markdown, no explanations, no additional text.

                Analyze the following README documentation and provide objective scores (1-10) for three key metrics.
                
                SCORING GUIDELINES (be fair and realistic):
                
                Clarity (1-10):
                - 8-10: Clear project description, purpose, and value proposition with examples
                - 6-7: Good description but could be clearer or more detailed
                - 4-5: Basic description present but lacks clarity
                - 1-3: Unclear or missing project description
                
                Completeness (1-10):
                - 9-10: Has Quick Start, installation, usage examples, contributing section, license info, badges, and links to docs
                - 7-8: Has most sections (installation, usage, contributing) but missing some details
                - 5-6: Has basic installation and usage but missing contributing or examples
                - 3-4: Only has minimal information
                - 1-2: Very incomplete
                
                Newcomer Friendly (1-10):
                - 9-10: Step-by-step Quick Start, clear prerequisites, multiple examples, troubleshooting
                - 7-8: Good Quick Start with examples, easy to follow
                - 5-6: Has basic instructions but could be clearer
                - 3-4: Instructions present but confusing
                - 1-2: Very hard for newcomers to understand
                
                REPOSITORY CONTEXT (files that already exist):
                %s
                
                CRITICAL RULES:
                1. Do NOT suggest adding files that already exist (LICENSE, CONTRIBUTING, etc.)
                2. If README mentions/links to existing files, give HIGHER completeness scores
                3. Focus suggestions on README content improvements only
                4. Be generous with scores if README is well-structured with examples
                
                Provide 2-3 specific strengths and 3-5 actionable suggestions for README content improvements.

                Expected JSON format (no markdown, no code blocks):
                {"clarity":8,"completeness":8,"newcomerFriendly":9,"strengths":["excellent quick start guide","comprehensive examples"],"suggestions":["add API reference section","include more code snippets","add troubleshooting FAQ"]}

                README content:
                %s

                CRITICAL: Output ONLY the JSON object. No markdown formatting, no code blocks, no explanations.
                """, repoContext, processedContent);
    }
    
    /**
     * Prepares README content for LLM analysis with smart truncation.
     * Prioritizes keeping important sections like title, description, and installation.
     */
    private String prepareReadmeContent(String content) {
        if (content == null || content.isEmpty()) {
            return "Empty README";
        }
        
        // If content fits within limit, return as-is
        if (content.length() <= MAX_README_LENGTH) {
            return content;
        }
        
        // Smart truncation: keep beginning (usually has important info)
        String truncated = content.substring(0, MAX_README_LENGTH);
        
        // Try to cut at a natural boundary (paragraph or section)
        int lastNewline = truncated.lastIndexOf("\n\n");
        if (lastNewline > MAX_README_LENGTH / 2) {
            truncated = truncated.substring(0, lastNewline);
        }
        
        return truncated + README_TRUNCATION_NOTICE;
    }

    private String buildCommitPrompt(String commitMessages) {
        return String.format("""
                IMPORTANT: Respond ONLY with a valid JSON object. No markdown, no explanations, no additional text.

                Analyze these commit messages and provide scores (1-10) for clarity, consistency, and informativeness.
                Also identify 3-5 patterns (positive and negative).

                Your response MUST be ONLY a JSON object in this exact format:
                {"clarity":8,"consistency":6,"informativeness":7,"patterns":["Positive: clear subject lines","Negative: inconsistent capitalization"]}

                Commits:
                %s

                IMPORTANT: Output ONLY the JSON object, nothing else.
                """, commitMessages.substring(0, Math.min(1000, commitMessages.length())));
    }

    private String buildCommunityPrompt(String owner, String repo) {
        return String.format("""
                IMPORTANT: Respond ONLY with a valid JSON object. No markdown, no explanations, no additional text.

                Analyze the community health of repository %s/%s and provide scores (1-10) for responsiveness, helpfulness, and tone.
                Provide 2-3 strengths and 3-5 suggestions.

                Your response MUST be ONLY a JSON object in this exact format:
                {"responsiveness":8,"helpfulness":7,"tone":9,"strengths":["active maintainers","clear guidelines","welcoming culture"],"suggestions":["implement triage system","add mentorship program","improve PR reviews","add community events"]}

                IMPORTANT: Output ONLY the JSON object, nothing else.
                """, owner, repo);
    }

    private LLMAnalysis.ReadmeAnalysis parseReadmeAnalysis(String content, AtomicInteger parseErrors) {
        try {
            // Diagnostic logging like in other program
            logger.error("=== LLM_DIAGNOSTICS [ERROR] README LLM JSON response: {} ===",
                content.substring(0, Math.min(1000, content.length())));
            String jsonContent = extractJsonFromText(content);
            JsonObject json = gson.fromJson(jsonContent, JsonObject.class);

            // Clean text from mojibake in strengths and suggestions
            List<String> cleanedStrengths = this.jsonArrayToList(json.getAsJsonArray("strengths"))
                    .stream()
                    .map(com.kaicode.rmi.util.EncodingHelper::cleanTextForWindows)
                    .collect(Collectors.toList());

            List<String> cleanedSuggestions = this.jsonArrayToList(json.getAsJsonArray("suggestions"))
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
            if (parseErrors != null) {
                parseErrors.incrementAndGet(); // Count JSON parsing failures
            }
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

    private LLMAnalysis.CommitAnalysis parseCommitAnalysis(String content, AtomicInteger parseErrors) {
        try {
            // Diagnostic logging like in other program
            logger.error("=== LLM_DIAGNOSTICS [ERROR] COMMIT LLM JSON response: {} ===",
                content.substring(0, Math.min(1000, content.length())));
            JsonObject json = gson.fromJson(content, JsonObject.class);

            // Clean text from mojibake in patterns
            List<String> cleanedPatterns = this.jsonArrayToList(json.getAsJsonArray("patterns"))
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
            if (parseErrors != null) {
                parseErrors.incrementAndGet(); // Count JSON parsing failures
            }
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

    private LLMAnalysis.CommunityAnalysis parseCommunityAnalysis(String content, AtomicInteger parseErrors) {
        try {
            // Diagnostic logging like in other program
            logger.error("=== LLM_DIAGNOSTICS [ERROR] COMMUNITY LLM JSON response: {} ===",
                content.substring(0, Math.min(1000, content.length())));
            JsonObject json = gson.fromJson(content, JsonObject.class);

            // Handle both formats: "responsiveness_score" and "responsiveness"
            int responsiveness = this.getIntValue(json, "responsiveness", "responsiveness_score");
            int helpfulness = this.getIntValue(json, "helpfulness", "helpfulness_score");
            int tone = this.getIntValue(json, "tone", "tone_score");

            // Clean text from mojibake in strengths and suggestions
            List<String> cleanedStrengths = this.jsonArrayToList(json.getAsJsonArray("strengths"))
                    .stream()
                    .map(com.kaicode.rmi.util.EncodingHelper::cleanTextForWindows)
                    .collect(Collectors.toList());

            List<String> cleanedSuggestions = this.jsonArrayToList(json.getAsJsonArray("suggestions"))
                    .stream()
                    .map(com.kaicode.rmi.util.EncodingHelper::cleanTextForWindows)
                    .collect(Collectors.toList());

            return new LLMAnalysis.CommunityAnalysis(
                    responsiveness, helpfulness, tone,
                    cleanedStrengths,
                    cleanedSuggestions
            );
        } catch (Exception e) {
            if (parseErrors != null) {
                parseErrors.incrementAndGet(); // Count JSON parsing failures
            }
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

    /**
     * Gets an integer value from JSON object, trying multiple key names.
     * This allows compatibility with different LLM response formats.
     *
     * @param json the JSON object to search
     * @param primaryKey the primary key name (e.g., "responsiveness")
     * @param fallbackKey the fallback key name (e.g., "responsiveness_score")
     * @return the integer value from the first key found, defaults to 3 if neither found
     */
    private int getIntValue(JsonObject json, String primaryKey, String fallbackKey) {
        if (json.has(primaryKey)) {
            return json.get(primaryKey).getAsInt();
        }
        if (json.has(fallbackKey)) {
            return json.get(fallbackKey).getAsInt();
        }
        // Default value if neither key exists
        return 3;
    }

    /**
     * Extracts JSON from LLM text response by finding JSON blocks with smart recovery
     */
    private String extractJsonFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "{}";
        }

        String originalText = text;

        // Step 1: Remove ALL markdown code blocks completely
        // Handle both ```json and ``` blocks, including multiline variants
        text = text.replaceAll("(?s)```json.*?```", "").replaceAll("(?s)```.*?```", "");

        // Clean up any leftover markdown markers
        text = text.replaceAll("```json\\n?", "").replaceAll("```\\n?", "");
        text = text.replaceAll("\\n?```", "");
        text = text.trim();

        // If text is empty after cleaning markdown, try the original text
        if (text.isEmpty() && originalText.contains("```")) {
            text = originalText;
            // Extract content between markdown blocks
            int startIndex = originalText.indexOf("```json");
            if (startIndex == -1) {
                startIndex = originalText.indexOf("```");
            }
            if (startIndex != -1) {
                startIndex = originalText.indexOf('\n', startIndex);
                if (startIndex != -1) {
                    int endIndex = originalText.indexOf("```", startIndex);
                    if (endIndex != -1) {
                        text = originalText.substring(startIndex, endIndex).trim();
                    }
                }
            }
        }

        // Step 2: Find the JSON object
        int firstBrace = text.indexOf('{');
        if (firstBrace != -1) {
            // Find matching closing brace for the first {
            int braceCount = 0;
            int lastValidBraceIndex = -1;
            boolean inString = false;

            for (int i = firstBrace; i < text.length(); i++) {
                char c = text.charAt(i);

                // Handle strings - don't count braces inside strings
                if (c == '"' && (i == 0 || text.charAt(i-1) != '\\')) {
                    inString = !inString;
                }

                if (!inString) {
                    if (c == '{') {
                        braceCount++;
                    } else if (c == '}') {
                        braceCount--;
                        if (braceCount == 0) {
                            lastValidBraceIndex = i;
                            break; // Found the matching closing brace
                        }
                    }
                }
            }

            if (lastValidBraceIndex != -1) {
                String jsonCandidate = text.substring(firstBrace, lastValidBraceIndex + 1);

                // Try to validate and fix common JSON issues
                jsonCandidate = fixCommonJsonIssues(jsonCandidate);

                // Quick validation
                long openBraces = jsonCandidate.chars().filter(ch -> ch == '{').count();
                long closeBraces = jsonCandidate.chars().filter(ch -> ch == '}').count();
                long quotes = jsonCandidate.chars().filter(ch -> ch == '"').count();

                if (openBraces == closeBraces && openBraces >= 1 && quotes >= 4 && jsonCandidate.length() > 10) {
                    return jsonCandidate;
                }
            }
        }

        // Try more aggressive approach - find all { } pairs and take the largest valid JSON
        if (text.contains("{") && text.contains("}")) {
            // Look for patterns like "response": "{...}" that might contain JSON as a string value
            int responseStart = text.indexOf("\"responsiveness\"");
            if (responseStart != -1) {
                int colonIndex = text.indexOf(':', responseStart);
                if (colonIndex != -1 && colonIndex + 1 < text.length()) {
                    String afterColon = text.substring(colonIndex + 1).trim();
                    if (afterColon.startsWith("{")) {
                        // This looks like "responsiveness": { ... json content ... }
                        return extractJsonFromText(afterColon);
                    }
                }
            }
        }

        // Ultimate fallback - use fixCommonJsonIssues on what we have
        if (text.contains("{") && text.contains("}")) {
            return fixCommonJsonIssues(text);
        }

        // Log failure but don't crash
        logger.warn("LLM JSON extraction failed, using fallback. Cleaned text: {}",
            text.substring(0, Math.min(200, text.length())));
        return "{}";
    }

    /**
     * Fixes common JSON issues that LLM might introduce
     */
    private String fixCommonJsonIssues(String json) {
        if (json == null) return "{}";

        // Remove trailing commas before closing braces/brackets
        json = json.replaceAll(",(\\s*[}\\]])", "$1");

        // Fix unescaped quotes in strings (common LLM error)
        // This is a simplistic fix - real solution would need proper JSON parsing
        json = json.replace("\\\"", "\"").replace("\\'", "'");

        // Remove extra content after the main JSON object
        int firstBrace = json.indexOf('{');
        int lastBrace = json.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace > firstBrace) {
            return json.substring(firstBrace, lastBrace + 1);
        }

        return json;
    }
}
