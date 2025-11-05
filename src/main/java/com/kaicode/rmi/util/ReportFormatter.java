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
        text.append("═══════════════════════════════════════════════════════════════\n");
        text.append("  Repository Maintainability Index Report\n");
        text.append("═══════════════════════════════════════════════════════════════\n\n");
        text.append("Repository: ").append(report.getRepositoryFullName()).append("\n");
        text.append("Overall Score: ").append(String.format("%.2f", report.getOverallScore())).append("/100\n");
        text.append("Rating: ").append(report.getRating()).append("\n\n");
        
        text.append("───────────────────────────────────────────────────────────────\n");
        text.append("  Detailed Metrics\n");
        text.append("───────────────────────────────────────────────────────────────\n\n");
        
        for (MetricResult metric : report.getMetrics().values()) {
            text.append(String.format("▪ %s: %.2f/100 (weight: %.0f%%)\n", 
                    metric.getName(), metric.getScore(), metric.getWeight() * 100));
            text.append("  ").append(metric.getDescription()).append("\n");
            if (metric.getDetails() != null && !metric.getDetails().isEmpty()) {
                text.append("  Details: ").append(metric.getDetails()).append("\n");
            }
            text.append("\n");
        }
        
        text.append("───────────────────────────────────────────────────────────────\n");
        text.append("  Recommendation\n");
        text.append("───────────────────────────────────────────────────────────────\n\n");
        text.append(report.getRecommendation()).append("\n");
        text.append("\n═══════════════════════════════════════════════════════════════\n");
        
        return text.toString();
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
