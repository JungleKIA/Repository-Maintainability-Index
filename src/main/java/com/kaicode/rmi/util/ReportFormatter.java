package com.kaicode.rmi.util;

import com.kaicode.rmi.model.MaintainabilityReport;
import com.kaicode.rmi.model.MetricResult;

import java.util.Map;

public class ReportFormatter {

    public String format(MaintainabilityReport report, OutputFormat format) {
        return switch (format) {
            case JSON -> formatJson(report);
            case TEXT -> formatText(report);
        };
    }

    private String formatJson(MaintainabilityReport report) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"repository\": \"").append(report.getRepositoryFullName()).append("\",\n");
        json.append("  \"overallScore\": ").append(String.format("%.2f", report.getOverallScore())).append(",\n");
        json.append("  \"rating\": \"").append(report.getRating()).append("\",\n");
        json.append("  \"metrics\": {\n");

        int count = 0;
        for (Map.Entry<String, MetricResult> entry : report.getMetrics().entrySet()) {
            MetricResult metric = entry.getValue();
            json.append("    \"").append(metric.getName()).append("\": {\n");
            json.append("      \"score\": ").append(String.format("%.2f", metric.getScore())).append(",\n");
            json.append("      \"weight\": ").append(String.format("%.2f", metric.getWeight())).append(",\n");
            json.append("      \"description\": \"").append(escapeJson(metric.getDescription())).append("\",\n");
            json.append("      \"details\": \"").append(escapeJson(metric.getDetails())).append("\"\n");
            json.append("    }");
            if (++count < report.getMetrics().size()) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  },\n");
        json.append("  \"recommendation\": \"").append(escapeJson(report.getRecommendation())).append("\"\n");
        json.append("}\n");

        return json.toString();
    }

    private String formatText(MaintainabilityReport report) {
        StringBuilder text = new StringBuilder();

        // Use box-drawing characters that work in Git Bash
        int width = 75;
        String topLine = "╔" + "═".repeat(width - 2) + "╗";
        String middleLine = "╠" + "═".repeat(width - 2) + "╣";
        String separator = "├" + "─".repeat(width - 2) + "┤";
        String bottomLine = "╚" + "═".repeat(width - 2) + "╝";

        // Header with emoji
        text.append("\n").append(topLine).append("\n");
        text.append(centerText("📊 Repository Maintainability Index Report", width)).append("\n");
        text.append(middleLine).append("\n\n");

        // Summary with visual rating
        text.append("📁 Repository: ").append(report.getRepositoryFullName()).append("\n");
        text.append("🎯 Overall Score: ").append(String.format("%.2f", report.getOverallScore())).append("/100 ");
        text.append(getScoreBar(report.getOverallScore())).append("\n");
        text.append("⭐ Rating: ").append(getRatingWithEmoji(report.getRating())).append("\n\n");

        // Detailed Metrics Section
        text.append(separator).append("\n");
        text.append(centerText("📈 Detailed Metrics", width)).append("\n");
        text.append(separator).append("\n\n");

        for (MetricResult metric : report.getMetrics().values()) {
            String emoji = getMetricEmoji(metric.getName());
            String scoreIndicator = getScoreIndicator(metric.getScore());
            
            text.append(String.format("%s %s: %.2f/100 %s (weight: %.0f%%)\n",
                    emoji, metric.getName(), metric.getScore(), scoreIndicator, metric.getWeight() * 100));
            text.append("   ").append(getScoreBar(metric.getScore())).append("\n");
            text.append("   ").append(metric.getDescription()).append("\n");
            if (metric.getDetails() != null && !metric.getDetails().isEmpty()) {
                text.append("   💬 ").append(metric.getDetails()).append("\n");
            }
            text.append("\n");
        }

        // Recommendation Section with visual priority
        text.append(separator).append("\n");
        text.append(centerText("💡 Recommendations", width)).append("\n");
        text.append(separator).append("\n\n");
        text.append(formatRecommendations(report)).append("\n");
        
        // Footer
        text.append(bottomLine).append("\n");

        return text.toString();
    }

    private String getMetricEmoji(String metricName) {
        return switch (metricName.toLowerCase()) {
            case "documentation" -> "📚";
            case "commit quality" -> "✍️";
            case "activity" -> "⚡";
            case "issue management" -> "🎫";
            case "community" -> "👥";
            case "branch management" -> "🌿";
            default -> "📊";
        };
    }

    private String getScoreIndicator(double score) {
        if (score >= 90) return "🟢";
        if (score >= 75) return "🟡";
        if (score >= 60) return "🟠";
        return "🔴";
    }

    private String getRatingWithEmoji(String rating) {
        return switch (rating.toUpperCase()) {
            case "EXCELLENT" -> "🌟 EXCELLENT";
            case "GOOD" -> "✅ GOOD";
            case "FAIR" -> "⚠️ FAIR";
            case "POOR" -> "❌ POOR";
            default -> rating;
        };
    }

    private String getScoreBar(double score) {
        int barLength = 20;
        int filled = (int) Math.round(score / 100.0 * barLength);
        String bar = "█".repeat(filled) + "░".repeat(barLength - filled);
        return "[" + bar + "]";
    }

    private String formatRecommendations(MaintainabilityReport report) {
        StringBuilder rec = new StringBuilder();
        String recommendation = report.getRecommendation();
        
        // Parse the recommendation text to add visual elements
        if (recommendation.contains("Excellent")) {
            rec.append("🎉 ").append(recommendation).append("\n");
            rec.append("   Keep up the outstanding work!");
        } else if (recommendation.contains("Good")) {
            rec.append("👍 ").append(recommendation);
        } else if (recommendation.contains("Fair")) {
            rec.append("⚠️ ").append(recommendation);
        } else {
            rec.append("🔧 ").append(recommendation);
        }
        
        // Add visual priority indicators for improvements
        if (recommendation.contains("Focus on improving:")) {
            rec.append("\n\n   Priority areas for improvement:");
            String[] parts = recommendation.split("Focus on improving:");
            if (parts.length > 1) {
                String improvements = parts[1].trim().replace(".", "");
                String[] items = improvements.split(",");
                int priority = 1;
                for (String item : items) {
                    String priorityEmoji = switch (priority) {
                        case 1 -> "🥇";
                        case 2 -> "🥈";
                        case 3 -> "🥉";
                        default -> "🔸";
                    };
                    rec.append("\n   ").append(priorityEmoji).append(" ").append(item.trim());
                    priority++;
                }
            }
        }
        
        return rec.toString();
    }

    private String centerText(String text, int width) {
        int padding = (width - text.length() - 2) / 2;
        String leftPad = " ".repeat(Math.max(0, padding));
        String rightPad = " ".repeat(Math.max(0, width - text.length() - padding - 2));
        return "║" + leftPad + text + rightPad + "║";
    }

    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public enum OutputFormat {
        JSON, TEXT
    }
}
