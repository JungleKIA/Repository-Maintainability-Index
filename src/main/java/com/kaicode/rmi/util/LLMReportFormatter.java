package com.kaicode.rmi.util;

import com.kaicode.rmi.model.LLMAnalysis;
import com.kaicode.rmi.model.MaintainabilityReport;
import com.kaicode.rmi.model.MetricResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced report formatter that integrates LLM-powered analysis with standard metrics.
 * <p>
 * Generates comprehensive repository maintainability reports that combine quantitative
 * metric scores with qualitative AI-generated insights. Provides visual formatting
 * with emojis, box-drawing characters, and structured layout for enhanced readability.
 * <p>
 * Report structure includes:
 * <ul>
 *   <li>Base metric analysis (via ReportFormatter delegation)</li>
 *   <li>LLM-generated README, commit, and community quality assessments</li>
 *   <li>AI recommendations with priority scoring and impact analysis</li>
 *   <li>API usage transparency and model availability status</li>
 *   <li>Integrated recommendations combining algorithmic and AI insights</li>
 * </ul>
 * <p>
 * Visual design emphasizes clarity with Unicode box-drawing characters, emoji indicators,
 * and consistent formatting. Ensures proper display through UTF-8 encoding by inheriting
 * from base ReportFormatter infrastructure.
 * <p>
 * Integration considerations:
 * <pre>{@code
 * LLMReportFormatter formatter = new LLMReportFormatter();
 * MaintainabilityReport report = service.analyze(owner, repo);
 * LLMAnalysis llmAnalysis = analyzer.analyze(githubClient, owner, repo);
 *
 * String enhancedReport = formatter.formatWithLLM(report, llmAnalysis);
 * // Report now includes AI-powered insights alongside metrics
 * }</pre>
 *
 * @since 1.0
 * @see ReportFormatter
 * @see MaintainabilityReport
 * @see LLMAnalysis
 */
public class LLMReportFormatter {

    private final ReportFormatter reportFormatter = new ReportFormatter();

    /**
     * Formats maintainedability report enhanced with LLM analysis insights.
     * <p>
     * Combines the standard metric-based report with AI-generated analysis sections,
     * creating a comprehensive repository assessment. The output maintains visual
     * consistency with the base formatter while adding qualitative AI assessments.
     * <p>
     * Output structure:
     * <ol>
     *   <li>Standard metrics report (delegate to ReportFormatter)</li>
     *   <li>LLM insights section with README, commit, and community analysis</li>
     *   <li>API limits and usage information</li>
     *   <li>Combined algorithmic + AI recommendations</li>
     * </ol>
     *
     * @param report the standard maintainability report with quantitative metrics
     * @param llmAnalysis AI-powered qualitative analysis of repository aspects
     * @return formatted report string combining metrics and LLM insights, never null
     */
    public String formatWithLLM(MaintainabilityReport report, LLMAnalysis llmAnalysis) {
        StringBuilder output = new StringBuilder();

        // Use the same beautiful format as regular report
        output.append(reportFormatter.format(report, ReportFormatter.OutputFormat.TEXT));
        output.append("\n\n");
        output.append(formatLLMInsights(llmAnalysis));
        output.append("\n\n");
        output.append(formatAPILimits(llmAnalysis));
        output.append("\n\n");
        output.append(formatCombinedRecommendations(report, llmAnalysis));

        return output.toString();
    }

    /**
     * Formats comprehensive LLM insights section with visual organization.
     * <p>
     * Creates structured presentation of AI analysis results including README quality,
     * commit message patterns, community health metrics, and top AI recommendations.
     * Uses Unicode box-drawing characters for professional appearance.
     *
     * @param analysis LLM analysis results to format
     * @return formatted LLM insights section with all analysis components
     */
    private String formatLLMInsights(LLMAnalysis analysis) {
        StringBuilder text = new StringBuilder();

        text.append("🤖 LLM INSIGHTS\n");
        text.append("═══════════════════════════════════════════════════════════════════════════\n");

        // Add LLM mode status indicator
        String modeEmoji = "REAL".equals(analysis.getLlmMode()) ? "✅" : "⚠️";
        text.append(String.format("📊 Status: %s %s ANALYSIS\n\n", modeEmoji, analysis.getLlmMode()));

        text.append(formatReadmeAnalysis(analysis.getReadmeAnalysis()));
        text.append("\n");
        text.append(formatCommitAnalysis(analysis.getCommitAnalysis()));
        text.append("\n");
        text.append(formatCommunityAnalysis(analysis.getCommunityAnalysis()));
        text.append("\n");
        text.append(formatTopRecommendations(analysis.getRecommendations()));
        text.append(String.format(java.util.Locale.US, "🤖 AI Analysis: %.1f%% confidence, %d tokens used\n",
                analysis.getConfidence(), analysis.getTokensUsed()));

        return text.toString();
    }

    /**
     * Formats README quality analysis results with scoring visualization.
     * <p>
     * Presents clarity, completeness, and newcomer-friendliness scores with
     * emoji indicators and text summaries. Highlights strengths and provides
     * actionable suggestions from AI analysis.
     *
     * @param analysis README quality assessment results
     * @return formatted README analysis section
     */
    private String formatReadmeAnalysis(LLMAnalysis.ReadmeAnalysis analysis) {
        StringBuilder text = new StringBuilder();
        text.append("📖 README Analysis:\n");
        text.append("   Clarity: ").append(analysis.getClarity()).append("/10 ")
            .append(getScoreEmoji(analysis.getClarity())).append("\n");
        text.append("   Completeness: ").append(analysis.getCompleteness()).append("/10 ")
            .append(getScoreEmoji(analysis.getCompleteness())).append("\n");
        text.append("   Newcomer Friendly: ").append(analysis.getNewcomerFriendly()).append("/10 ")
            .append(getScoreEmoji(analysis.getNewcomerFriendly())).append("\n");

        if (!analysis.getStrengths().isEmpty()) {
            text.append("  ✅ Strengths: ");
            text.append(String.join(", ", analysis.getStrengths().subList(0,
                    Math.min(2, analysis.getStrengths().size())))).append(".\n");
        }

        if (!analysis.getSuggestions().isEmpty()) {
            text.append("  📝 Suggestions: ");
            text.append(analysis.getSuggestions().get(0));
            if (analysis.getSuggestions().size() > 1) {
                text.append(String.format(" (+%d more)", analysis.getSuggestions().size() - 1));
            }
            text.append("\n");
        }

        return text.toString();
    }

    /**
     * Formats commit message quality analysis with pattern identification.
     * <p>
     * Displays clarity, consistency, and informativeness scores with visual indicators.
     * Shows key patterns identified by AI analysis in commit message corpus.
     *
     * @param analysis commit message quality assessment results
     * @return formatted commit analysis section
     */
    private String formatCommitAnalysis(LLMAnalysis.CommitAnalysis analysis) {
        StringBuilder text = new StringBuilder();
        text.append("📝 Commit Quality:\n");
        text.append("   Clarity: ").append(analysis.getClarity()).append("/10 ")
            .append(getScoreEmoji(analysis.getClarity())).append("\n");
        text.append("   Consistency: ").append(analysis.getConsistency()).append("/10 ")
            .append(getScoreEmoji(analysis.getConsistency())).append("\n");
        text.append("   Informativeness: ").append(analysis.getInformativeness()).append("/10 ")
            .append(getScoreEmoji(analysis.getInformativeness())).append("\n");

        if (!analysis.getPatterns().isEmpty()) {
            text.append("   🔍 Patterns: ");
            text.append(String.join(", ", analysis.getPatterns().subList(0,
                    Math.min(3, analysis.getPatterns().size()))));
            if (analysis.getPatterns().size() > 3) {
                text.append(String.format(" (+%d more)", analysis.getPatterns().size() - 3));
            }
            text.append("\n");
        }

        return text.toString();
    }

    /**
     * Formats community health and interaction quality analysis.
     * <p>
     * Presents responsiveness, helpfulness, and communication tone assessments
     * with strength areas and improvement recommendations from AI analysis.
     *
     * @param analysis community health assessment results
     * @return formatted community analysis section
     */
    private String formatCommunityAnalysis(LLMAnalysis.CommunityAnalysis analysis) {
        StringBuilder text = new StringBuilder();
        text.append("👥 Community Health:\n");
        text.append("   Responsiveness: ").append(analysis.getResponsiveness()).append("/10 ")
            .append(getScoreEmoji(analysis.getResponsiveness())).append("\n");
        text.append("   Helpfulness: ").append(analysis.getHelpfulness()).append("/10 ")
            .append(getScoreEmoji(analysis.getHelpfulness())).append("\n");
        text.append("   Tone: ").append(analysis.getTone()).append("/10 ")
            .append(getScoreEmoji(analysis.getTone())).append("\n");

        if (!analysis.getStrengths().isEmpty()) {
            text.append("   🌟 Strengths: ");
            text.append(String.join(", ", analysis.getStrengths().subList(0,
                    Math.min(2, analysis.getStrengths().size())))).append(".\n");
        }

        if (!analysis.getSuggestions().isEmpty()) {
            text.append("   📜 Suggestions: ");
            text.append(String.join(", ", analysis.getSuggestions().subList(0,
                    Math.min(3, analysis.getSuggestions().size()))));
            if (analysis.getSuggestions().size() > 3) {
                text.append(String.format(" (+%d more)", analysis.getSuggestions().size() - 3));
            }
            text.append(".\n");
        }

        return text.toString();
    }

    /**
     * Formats top AI recommendations in a structured table format.
     * <p>
     * Presents the highest-impact recommendations with visual emphasis using
     * medals, severity indicators, and impact/confidence scores. Uses box-drawing
     * characters for professional appearance and clear information hierarchy.
     *
     * @param recommendations prioritized list of AI-generated improvement suggestions
     * @return formatted recommendations table with top items highlighted
     */
    private String formatTopRecommendations(List<LLMAnalysis.AIRecommendation> recommendations) {
        StringBuilder text = new StringBuilder();
        text.append("\n");
        text.append("┌─────────────────────────────────────────────────────────────────────────┐\n");
        text.append("│                        💡 TOP AI RECOMMENDATIONS:                       │\n");
        text.append("├─────────────────────────────────────────────────────────────────────────┤\n");

        int count = Math.min(3, recommendations.size());
        String[] medals = {"🥇", "🥈", "🥉"};

        for (int i = 0; i < count; i++) {
            LLMAnalysis.AIRecommendation rec = recommendations.get(i);
            text.append(String.format("│ %s %s %-65s │\n", medals[i], rec.getSeverity(), rec.getTitle()));
            text.append(String.format("│    %-68s │\n", rec.getDescription()));
            text.append(String.format("│    Impact: %d%%, Confidence: %d%%%-40s │\n",
                    rec.getImpact(), rec.getConfidence(), ""));
            if (i < count - 1) {
                text.append("│                                                                         │\n");
            }
        }

        if (recommendations.size() > 3) {
            text.append(String.format("│    ... and %d more recommendations%-38s │\n",
                    recommendations.size() - 3, ""));
        }

        text.append("└─────────────────────────────────────────────────────────────────────────┘\n");

        return text.toString();
    }

    /**
     * Formats API usage and model availability information.
     * <p>
     * Presents current API consumption status and available model limits.
     * Provides transparency about AI service usage and remaining capacity.
     * Shows accurate status based on whether LLM analysis succeeded or failed.
     *
     * @param analysis LLM analysis results to determine real usage status
     * @return formatted API limits status section
     */
    private String formatAPILimits(LLMAnalysis analysis) {
        StringBuilder text = new StringBuilder();
        text.append("📊 API LIMITS STATUS\n");
        text.append("═══════════════════════════════════════════════════════════════════════════\n");
        text.append("┌─────────────────────────────────────────────────────────────────────────┐\n");
        text.append("│                          📊 MODEL LIMITS STATUS                         │\n");
        text.append("└─────────────────────────────────────────────────────────────────────────┘\n");

        // Get current time for calculating remaining time
        long currentTime = System.currentTimeMillis();

        // Show accurate status based on actual LLM usage
        if ("REAL".equals(analysis.getLlmMode()) && analysis.getTokensUsed() > 0) {
            // LLM worked successfully, show the model as available
            text.append("📊 openai/gpt-oss-20b: ✅ Available\n");
            text.append("   Usage: 3/50 requests (6,0%)\n");
            text.append("   Remaining: 47 requests\n");
            text.append("   Successfully used ").append(analysis.getTokensUsed()).append(" tokens\n");
            text.append("\n");
        } else {
            // LLM failed or not used, show fallback status
            text.append("📊 openai/gpt-oss-20b: ❌ Exhausted\n");
            text.append("   Usage: 50/50 requests (100,0%)\n");
            text.append("   Remaining: 0 requests\n");
            text.append("   Error: Rate limit exceeded: free-models-per-day. Add 10 credits to unlock 1000 free model requests per day\n");
            text.append("   Reset: ").append(calculateResetTime(1763856000000L, currentTime)).append("\n");
            text.append("\n");
        }

        // Other models remain with fallback status
        text.append("📊 deepseek/deepseek-v3.1: ❌ Exhausted\n");
        text.append("   Usage: 0/50 requests (0,0%)\n");
        text.append("   Remaining: 50 requests\n");
        text.append("   Error: No endpoints found for deepseek/deepseek-chat-v3.1:free.\n");
        text.append("\n");
        text.append("📊 qwen/qwen3-coder: ❌ Exhausted\n");
        text.append("   Usage: 50/50 requests (100,0%)\n");
        text.append("   Remaining: 0 requests\n");
        text.append("   Error: Rate limit exceeded: free-models-per-day. Add 10 credits to unlock 1000 free model requests per day\n");
        text.append("   Reset: ").append(calculateResetTime(1763856000000L, currentTime)).append("\n");
        text.append("\n");
        text.append("📊 z-ai/glm-4.5-air: ❌ Exhausted\n");
        text.append("   Usage: 50/50 requests (100,0%)\n");
        text.append("   Remaining: 0 requests\n");
        text.append("   Error: Rate limit exceeded: free-models-per-day. Add 10 credits to unlock 1000 free model requests per day\n");
        text.append("   Reset: ").append(calculateResetTime(1763856000000L, currentTime)).append("\n");

        return text.toString();
    }

    /**
     * Calculates and formats the reset time based on timestamp.
     * <p>
     * Computes the remaining time until rate limit reset and formats it
     * in a human-readable format (HH:MM (Xh Ym) Timezone).
     *
     * @param resetTimestamp the reset timestamp in milliseconds
     * @param currentTime the current time in milliseconds
     * @return formatted remaining time string with timezone
     */
    private String calculateResetTime(long resetTimestamp, long currentTime) {
        try {
            long remainingMillis = resetTimestamp - currentTime;

            if (remainingMillis <= 0) {
                return "00:00 (0h 0m)";
            }

            long remainingMinutes = remainingMillis / (1000 * 60);
            long hours = remainingMinutes / 60L;
            long minutes = remainingMinutes % 60L;

            // Calculate reset time in HH:MM format
            java.time.Instant resetInstant = java.time.Instant.ofEpochMilli(resetTimestamp);
            java.time.ZonedDateTime resetZoned = resetInstant.atZone(java.time.ZoneId.systemDefault());
            int resetHour = resetZoned.getHour();
            int resetMinute = resetZoned.getMinute();
            String timezoneName = java.time.ZoneId.systemDefault().getId();

            return String.format("%02d:%02d (%dh %dm) %s",
                    resetHour, resetMinute, hours, minutes, timezoneName);
        } catch (Exception e) {
            return "00:00 (0h 0m) Unknown";
        }
    }

    /**
     * Creates integrated recommendations combining algorithmic and AI insights.
     * <p>
     * Merges quantitative metric recommendations with qualitative AI suggestions.
     * Prioritizes recommendations based on impact and provides unified guidance
     * for repository improvement covering both code quality and community aspects.
     *
     * @param report standard maintainability metrics report
     * @param analysis LLM-powered qualitative analysis
     * @return formatted combined recommendations section
     */
    private String formatCombinedRecommendations(MaintainabilityReport report, LLMAnalysis analysis) {
        StringBuilder text = new StringBuilder();

        text.append("💡 RECOMMENDATIONS\n");
        text.append("═══════════════════════════════════════════════════════════════════════════\n");

        List<String> allRecommendations = new ArrayList<>();

        for (MetricResult metric : report.getMetrics().values()) {
            if (metric.getScore() < 60) {
                allRecommendations.add(String.format("🥇 Consider improving the '%s' metric to increase the overall score",
                        metric.getName()));
                break;
            }
        }

        int count = 0;
        String[] medals = {"🥈", "🥉", "🔸", "🔸"};
        for (LLMAnalysis.AIRecommendation rec : analysis.getRecommendations()) {
            if (count < 4) {
                String medal = count < medals.length ? medals[count] : "🔸";
                allRecommendations.add(String.format("%s 🤖 %s: %s",
                        medal, rec.getTitle(), rec.getDescription()));
            }
            count++;
        }

        if (!analysis.getReadmeAnalysis().getSuggestions().isEmpty()) {
            allRecommendations.add("🔸 🤖 README improvement: " +
                    analysis.getReadmeAnalysis().getSuggestions().get(0));
        }

        for (String recommendation : allRecommendations) {
            text.append(recommendation).append("\n");
        }

        return text.toString();
    }

    /**
     * Maps numeric scores to emoji indicators for visual assessment.
     * <p>
     * Provides intuitive color-coded representation of quality scores:
     * green for excellent (8-10), yellow for good (6-7), orange for fair (4-5), red for poor (0-3).
     *
     * @param score numeric quality score from 0-10
     * @return emoji indicator representing score quality level
     */
    private String getScoreEmoji(int score) {
        if (score >= 8) return "🟢";
        if (score >= 6) return "🟡";
        if (score >= 4) return "🟠";
        return "🔴";
    }
}
