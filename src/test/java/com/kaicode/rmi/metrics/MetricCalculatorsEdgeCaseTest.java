package com.kaicode.rmi.metrics;

import com.kaicode.rmi.github.GitHubClient;
import com.kaicode.rmi.model.CommitInfo;
import com.kaicode.rmi.model.MetricResult;
import com.kaicode.rmi.model.RepositoryInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricCalculatorsEdgeCaseTest {

    @Mock
    private GitHubClient client;

    @Test
    void activityMetricShouldHandleExactBoundaries() {
        ActivityMetric metric = new ActivityMetric();
        
        assertThat(metric.calculateScoreFromDays(7)).isEqualTo(100.0);
        assertThat(metric.calculateScoreFromDays(8)).isEqualTo(90.0);
        assertThat(metric.calculateScoreFromDays(30)).isEqualTo(90.0);
        assertThat(metric.calculateScoreFromDays(31)).isEqualTo(70.0);
        assertThat(metric.calculateScoreFromDays(90)).isEqualTo(70.0);
        assertThat(metric.calculateScoreFromDays(91)).isEqualTo(50.0);
        assertThat(metric.calculateScoreFromDays(180)).isEqualTo(50.0);
        assertThat(metric.calculateScoreFromDays(181)).isEqualTo(30.0);
        assertThat(metric.calculateScoreFromDays(365)).isEqualTo(30.0);
        assertThat(metric.calculateScoreFromDays(366)).isEqualTo(10.0);
    }

    @Test
    void branchMetricShouldHandleExactBoundaries() {
        BranchManagementMetric metric = new BranchManagementMetric();
        
        assertThat(metric.calculateScore(1)).isEqualTo(100.0);
        assertThat(metric.calculateScore(3)).isEqualTo(100.0);
        assertThat(metric.calculateScore(4)).isEqualTo(95.0);
        assertThat(metric.calculateScore(5)).isEqualTo(95.0);
        assertThat(metric.calculateScore(6)).isEqualTo(85.0);
        assertThat(metric.calculateScore(10)).isEqualTo(85.0);
        assertThat(metric.calculateScore(11)).isEqualTo(70.0);
        assertThat(metric.calculateScore(20)).isEqualTo(70.0);
        assertThat(metric.calculateScore(21)).isEqualTo(50.0);
        assertThat(metric.calculateScore(50)).isEqualTo(50.0);
        assertThat(metric.calculateScore(51)).isEqualTo(30.0);
    }

    @Test
    void issueMetricShouldHandleExactBoundaries() {
        IssueManagementMetric metric = new IssueManagementMetric();
        
        assertThat(metric.calculateScore(80, 5)).isGreaterThanOrEqualTo(95.0);
        assertThat(metric.calculateScore(79, 5)).isLessThan(95.0);
        assertThat(metric.calculateScore(60, 5)).isGreaterThanOrEqualTo(80.0);
        assertThat(metric.calculateScore(59, 5)).isLessThan(80.0);
        assertThat(metric.calculateScore(40, 5)).isGreaterThanOrEqualTo(65.0);
        assertThat(metric.calculateScore(39, 5)).isLessThan(65.0);
        assertThat(metric.calculateScore(20, 5)).isGreaterThanOrEqualTo(45.0);
        assertThat(metric.calculateScore(19, 5)).isLessThan(45.0);
    }

    @Test
    void issueMetricShouldHandleOpenIssuesBoundaries() {
        IssueManagementMetric metric = new IssueManagementMetric();
        
        double score50 = metric.calculateScore(80, 50);
        double score51 = metric.calculateScore(80, 51);
        double score100 = metric.calculateScore(80, 100);
        double score101 = metric.calculateScore(80, 101);
        
        assertThat(score50).isGreaterThan(score51);
        assertThat(score100).isGreaterThan(score101);
    }

    @Test
    void communityMetricShouldHandleZeroValues() {
        CommunityMetric metric = new CommunityMetric();
        
        double score = metric.calculateScore(0, 0, 0);
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    void communityMetricShouldCapScoresAtMaximum() {
        CommunityMetric metric = new CommunityMetric();
        
        double starScore = metric.calculateScore(2000, 0, 0);
        double forkScore = metric.calculateScore(0, 1000, 0);
        double contributorScore = metric.calculateScore(0, 0, 20);
        
        assertThat(starScore).isLessThanOrEqualTo(100.0);
        assertThat(forkScore).isLessThanOrEqualTo(100.0);
        assertThat(contributorScore).isLessThanOrEqualTo(100.0);
    }

    @Test
    void commitQualityMetricShouldHandleMultilineMessages() {
        CommitQualityMetric metric = new CommitQualityMetric();
        
        assertThat(metric.isGoodCommit("feat: add feature")).isTrue();
        assertThat(metric.isGoodCommit("fix: bug fix implementation")).isTrue();
    }

    @Test
    void commitQualityMetricShouldHandleConventionalCommitsWithScope() {
        CommitQualityMetric metric = new CommitQualityMetric();
        
        assertThat(metric.isGoodCommit("feat(api): add endpoint")).isTrue();
        assertThat(metric.isGoodCommit("fix(ui): button color")).isTrue();
        assertThat(metric.isGoodCommit("docs(readme): update install")).isTrue();
    }

    @Test
    void commitQualityMetricShouldRejectLowercaseStart() {
        CommitQualityMetric metric = new CommitQualityMetric();
        
        assertThat(metric.isGoodCommit("updated the documentation file")).isFalse();
    }

    @Test
    void commitQualityMetricShouldRejectMergeCommits() {
        CommitQualityMetric metric = new CommitQualityMetric();
        
        assertThat(metric.isGoodCommit("Merge pull request #123")).isFalse();
        assertThat(metric.isGoodCommit("merge branch 'develop'")).isFalse();
    }

    @Test
    void commitQualityMetricShouldRejectWipCommits() {
        CommitQualityMetric metric = new CommitQualityMetric();
        
        assertThat(metric.isGoodCommit("wip: testing")).isFalse();
        assertThat(metric.isGoodCommit("WIP - in progress")).isFalse();
    }

    @Test
    void documentationMetricShouldDetectAllFiles() throws IOException {
        DocumentationMetric metric = new DocumentationMetric();
        
        when(client.hasFile("owner", "repo", "README.md")).thenReturn(true);
        when(client.hasFile("owner", "repo", "CONTRIBUTING.md")).thenReturn(true);
        when(client.hasFile("owner", "repo", "LICENSE")).thenReturn(true);
        when(client.hasFile("owner", "repo", "CODE_OF_CONDUCT.md")).thenReturn(true);
        when(client.hasFile("owner", "repo", "CHANGELOG.md")).thenReturn(true);
        
        MetricResult result = metric.calculate(client, "owner", "repo");
        
        assertThat(result.getDetails()).contains("README.md");
        assertThat(result.getDetails()).contains("CONTRIBUTING.md");
        assertThat(result.getDetails()).contains("LICENSE");
        assertThat(result.getDetails()).contains("CODE_OF_CONDUCT.md");
        assertThat(result.getDetails()).contains("CHANGELOG.md");
        assertThat(result.getDetails()).contains("Missing: none");
    }

    @Test
    void documentationMetricShouldShowMissingFiles() throws IOException {
        DocumentationMetric metric = new DocumentationMetric();
        
        when(client.hasFile("owner", "repo", "README.md")).thenReturn(false);
        when(client.hasFile("owner", "repo", "CONTRIBUTING.md")).thenReturn(false);
        when(client.hasFile("owner", "repo", "LICENSE")).thenReturn(false);
        when(client.hasFile("owner", "repo", "CODE_OF_CONDUCT.md")).thenReturn(false);
        when(client.hasFile("owner", "repo", "CHANGELOG.md")).thenReturn(false);
        
        MetricResult result = metric.calculate(client, "owner", "repo");
        
        assertThat(result.getDetails()).contains("Found: none");
        assertThat(result.getDetails()).contains("README.md");
        assertThat(result.getDetails()).contains("CONTRIBUTING.md");
    }

    @Test
    void activityMetricShouldHandleVeryRecentCommit() throws IOException {
        ActivityMetric metric = new ActivityMetric();
        
        LocalDateTime now = LocalDateTime.now();
        List<CommitInfo> commits = List.of(
                CommitInfo.builder()
                        .sha("abc")
                        .message("Recent")
                        .date(now)
                        .build()
        );
        
        when(client.getRecentCommits("owner", "repo", 10)).thenReturn(commits);
        
        MetricResult result = metric.calculate(client, "owner", "repo");
        assertThat(result.getScore()).isEqualTo(100.0);
    }
}
