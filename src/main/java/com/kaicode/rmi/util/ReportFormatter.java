package com.kaicode.rmi.util;

import com.kaicode.rmi.model.MaintainabilityReport;
import com.kaicode.rmi.model.MetricResult;

import java.util.Map;

/**
 * Professional report formatter for maintainability analysis results.
 * <p>
 * Formats repository maintainability reports in multiple output formats with
 * visual enhancements for enhanced readability and professional presentation.
 * Specializes in text-based formatting with Unicode box-drawing characters,
 * emoji indicators, and progress visualization for console and document display.
 * <p>
 * Supports output formats:
 * <ul>
 *   <li><strong>TEXT</strong>: Human-readable formatted reports with visual elements</li>
 *   <li><strong>JSON</strong>: Machine-readable structured data for integration</li>
 * </ul>
 * <p>
 * Text format features:
 * <ul>
 *   <li>Unicode box-drawing characters for structured layout</li>
 *   <li>Color-coded progress bars and score indicators</li>
 *   <li>Contextual emojis for metric types and ratings</li>
 *   <li>Priority-based recommendation formatting</li>
 *   <li>Responsive visual design for different terminal widths</li>
 * </ul>
 * <p>
 * Visual design ensures compatibility with UTF-8 enabled terminals (GitBash, modern consoles)
 * while maintaining readability in basic text environments through strategic Unicode usage.
 * <p>
 * Typical usage:
 * <pre>{@code
 * ReportFormatter formatter = new ReportFormatter();
 *
 * // Console display
 * String textReport = formatter.format(report, ReportFormatter.OutputFormat.TEXT);
 * System.out.println(textReport);
 *
 * // Programmatic access
 * String jsonReport = formatter.format(report, ReportFormatter.OutputFormat.JSON);
 * // parse and use JSON data
 * }</pre>
 *
 * @since 1.0
 * @see MaintainabilityReport
 * @see MetricResult
 * @see LLMReportFormatter
 */
public class ReportFormatter {

    /**
     * Formats maintainability report in the specified output format.
     * <p>
     * Dispatcher method that routes formatting requests to appropriate
     * format-specific implementations. Currently supports TEXT and JSON formats.
     * The method ensures consistent data integrity across all output formats.
     *
     * @param report maintainability analysis results to format
     * @param format desired output format (TEXT or JSON)
     * @return formatted report string, never null
     * @since 1.0
     */
    public String format(MaintainabilityReport report, OutputFormat format) {
        return switch (format) {
            case JSON -> formatJson(report);
            case TEXT -> formatText(report);
        };
    }

    /**
     * Formats report as structured JSON for machine consumption.
     * <p>
     * Produces machine-readable JSON representation with properly escaped
     * strings and formatted numeric values. Designed for integration with
     * external systems, APIs, and automated processing workflows.
     * <p>
     * JSON structure mirrors the MaintainabilityReport object model
     * with nested metrics array and escaped string content.
     *
     * @param report report data to serialize as JSON
     * @return valid JSON string representation of report data
     */
    private String formatJson(MaintainabilityReport report) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"repository\": \"").append(report.getRepositoryFullName()).append("\",\n");
        json.append("  \"overallScore\": ").append(String.format(java.util.Locale.US, "%.2f", report.getOverallScore())).append(",\n");
        json.append("  \"rating\": \"").append(report.getRating()).append("\",\n");
        json.append("  \"metrics\": {\n");

        int count = 0;
        for (Map.Entry<String, MetricResult> entry : report.getMetrics().entrySet()) {
            MetricResult metric = entry.getValue();
            json.append("    \"").append(metric.getName()).append("\": {\n");
            json.append("      \"score\": ").append(String.format(java.util.Locale.US, "%.2f", metric.getScore())).append(",\n");
            json.append("      \"weight\": ").append(String.format(java.util.Locale.US, "%.2f", metric.getWeight())).append(",\n");
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

    /**
     * Formats report as visually enhanced text for human consumption.
     * <p>
     * Creates professional console-formatted report with Unicode elements,
     * progress bars, emoji indicators, and structured layout. Uses box-drawing
     * characters for borders and sections, ensuring visual appeal while
     * maintaining compatibility with UTF-8 enabled terminals.
     * <p>
     * Layout structure:
     * <ol>
     *   <li>Header with repository identification and overall score</li>
     *   <li>Detailed metrics section with individual progress bars</li>
     *   <li>Recommendations section with priority-based formatting</li>
     *   <li>Footer with professional presentation</li>
     * </ol>
     *
     * @param report report data to format as human-readable text
     * @return formatted text report with visual enhancements
     */
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
        text.append("🎯 Overall Score: ").append(String.format(java.util.Locale.US, "%.2f", report.getOverallScore())).append("/100 ");
        text.append(getScoreBar(report.getOverallScore())).append("\n");
        text.append("⭐ Rating: ").append(getRatingWithEmoji(report.getRating())).append("\n\n");

        // Detailed Metrics Section
        text.append(separator).append("\n");
        text.append(centerText("📈 Detailed Metrics", width)).append("\n");
        text.append(separator).append("\n\n");

        for (MetricResult metric : report.getMetrics().values()) {
            String emoji = getMetricEmoji(metric.getName());
            String scoreIndicator = getScoreIndicator(metric.getScore());

            text.append(String.format(java.util.Locale.US, "%s %s: %.2f/100 %s (weight: %.0f%%)\n",
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

    /**
     * Returns appropriate emoji for metric type identification.
     * <p>
     * Provides visual differentiation between metric categories through
     * contextually relevant emoji symbols. Helps users quickly identify
     * different aspects of repository health assessment.
     *
     * @param metricName name of the metric to get emoji for
     * @return relevant emoji symbol for the metric type
     */
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

    /**
     * Maps numeric scores to color-coded emoji indicators.
     * <p>
     * Translates score ranges to intuitive visual indicators:
     * 🟢 (green) for excellent performance (90-100),
     * 🟡 (yellow) for good performance (75-89),
     * 🟠 (orange) for fair performance (60-74),
     * 🔴 (red) for poor performance (0-59).
     *
     * @param score numeric score from 0.0 to 100.0
     * @return emoji indicator representing performance level
     */
    private String getScoreIndicator(double score) {
        if (score >= 90)
            return "🟢";
        if (score >= 75)
            return "🟡";
        if (score >= 60)
            return "🟠";
        return "🔴";
    }

    /**
     * Enhances rating text with appropriate emoji indicators.
     * <p>
     * Converts plain text ratings to visually enhanced versions with
     * corresponding emojis for immediate recognition of repository health status.
     *
     * @param rating plain text rating (EXCELLENT, GOOD, FAIR, POOR)
     * @return enhanced rating text with emoji prefix
     */
    private String getRatingWithEmoji(String rating) {
        return switch (rating.toUpperCase()) {
            case "EXCELLENT" -> "🌟 EXCELLENT";
            case "GOOD" -> "✅ GOOD";
            case "FAIR" -> "⚠️ FAIR";
            case "POOR" -> "❌ POOR";
            default -> rating;
        };
    }

    /**
     * Generates visual progress bar representation of scores.
     * <p>
     * Creates visual bar chart using Unicode block elements showing
     * relative score achievement. Uses filled blocks (█) for achieved
     * portion and empty blocks (░) for remaining portion.
     *
     * @param score score value from 0.0 to 100.0
     * @return visual progress bar string enclosed in brackets
     */
    private String getScoreBar(double score) {
        int barLength = 20;
        int filled = (int) Math.round(score / 100.0 * barLength);
        String bar = "█".repeat(filled) + "░".repeat(barLength - filled);
        return "[" + bar + "]";
    }

    /**
     * Formats recommendation text with visual enhancements and priority structure.
     * <p>
     * Processes raw recommendation text to add contextual emojis and structured
     * priority formatting. Identifies key improvement areas and presents them
     * with medal emojis for prioritization. Handles different recommendation
     * types (excellent, good, fair, poor) with appropriate visual styling.
     *
     * @param report report containing recommendation text to format
     * @return enhanced recommendation text with visual elements
     */
    private String formatRecommendations(MaintainabilityReport report) {
        StringBuilder rec = new StringBuilder();
        String recommendation = report.getRecommendation();

        // Handle null or empty recommendation
        if (recommendation == null || recommendation.isEmpty()) {
            return "No recommendations available.";
        }

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

    /**
     * Centers text within specified width using box-drawing borders.
     * <p>
     * Creates visually centered text sections with Unicode box-drawing
     * characters for use as headers and section dividers. Calculates
     * appropriate padding to achieve center alignment within the given width.
     *
     * @param text text content to center
     * @param width total width including border characters
     * @return centered text enclosed in box-drawing border characters
     */
    private String centerText(String text, int width) {
        int padding = (width - text.length() - 2) / 2;
        String leftPad = " ".repeat(Math.max(0, padding));
        String rightPad = " ".repeat(Math.max(0, width - text.length() - padding - 2));
        return "║" + leftPad + text + rightPad + "║";
    }

    /**
     * Escapes JSON special characters in strings.
     * <p>
     * Properly escapes backslashes, quotes, and control characters
     * to ensure valid JSON output. Handles newlines, tabs, and other
     * special sequences that could break JSON parsing.
     *
     * @param str string to escape for JSON inclusion
     * @return JSON-safe string with special characters escaped
     */
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

    /**
     * Supported output format types for report formatting.
     * <p>
     * Defines available serialization formats for maintainability reports.
     * Current supported formats provide both human-readable and machine-processable outputs.
     *
     * @since 1.0
     */
    public enum OutputFormat {
        /**
         * Structured JSON format for machine consumption and API integration.
         * Produces valid JSON with proper escaping and numeric formatting.
         */
        JSON,

        /**
         * Enhanced text format with visual elements for human consumption.
         * Includes emoji indicators, progress bars, and Unicode formatting.
         */
        TEXT
    }
}
