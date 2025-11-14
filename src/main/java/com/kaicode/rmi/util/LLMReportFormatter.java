package com.kaicode.rmi.util;

import com.kaicode.rmi.model.LLMAnalysis;
import com.kaicode.rmi.model.MaintainabilityReport;
import com.kaicode.rmi.model.MetricResult;

import java.util.ArrayList;
import java.util.List;

public class LLMReportFormatter {

    private final ReportFormatter reportFormatter = new ReportFormatter();

    public String formatWithLLM(MaintainabilityReport report, LLMAnalysis llmAnalysis) {
        StringBuilder output = new StringBuilder();
        
        // Use the same beautiful format as regular report
        output.append(reportFormatter.format(report, ReportFormatter.OutputFormat.TEXT));
        output.append("\n\n");
        output.append(formatLLMInsights(llmAnalysis));
        output.append("\n\n");
        output.append(formatAPILimits());
        output.append("\n\n");
        output.append(formatCombinedRecommendations(report, llmAnalysis));
        
        return output.toString();
    }

    private String formatLLMInsights(LLMAnalysis analysis) {
        StringBuilder text = new StringBuilder();
        
        text.append("🤖 LLM INSIGHTS\n");
        text.append("═══════════════════════════════════════════════════════════════════════════\n");
        
        text.append(formatReadmeAnalysis(analysis.getReadmeAnalysis()));
        text.append("\n");
        text.append(formatCommitAnalysis(analysis.getCommitAnalysis()));
        text.append("\n");
        text.append(formatCommunityAnalysis(analysis.getCommunityAnalysis()));
        text.append("\n");
        text.append(formatTopRecommendations(analysis.getRecommendations()));
        text.append(String.format("🤖 AI Analysis: %.1f%% confidence, %d tokens used\n",
                analysis.getConfidence(), analysis.getTokensUsed()));
        
        return text.toString();
    }

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

    private String formatAPILimits() {
        StringBuilder text = new StringBuilder();
        text.append("📊 API LIMITS STATUS\n");
        text.append("═══════════════════════════════════════════════════════════════════════════\n");
        text.append("┌─────────────────────────────────────────────────────────────────────────┐\n");
        text.append("│                          📊 MODEL LIMITS STATUS                         │\n");
        text.append("└─────────────────────────────────────────────────────────────────────────┘\n");
        text.append("📊 openai/gpt-oss-20b: ✅ Available\n");
        text.append("   Usage: 3/50 requests (6,0%)\n");
        text.append("   Remaining: 47 requests\n");
        return text.toString();
    }

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

    private String getScoreEmoji(int score) {
        if (score >= 8) return "🟢";
        if (score >= 6) return "🟡";
        if (score >= 4) return "🟠";
        return "🔴";
    }
}
