package com.kaicode.rmi.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelBranchCoverageTest {

    @Test
    void repositoryInfoShouldHandleNullComparisons() {
        RepositoryInfo repo = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .build();

        assertThat(repo.equals(null)).isFalse();
        assertThat(repo.equals("not a repo")).isFalse();
        assertThat(repo.equals(repo)).isTrue();
    }

    @Test
    void metricResultShouldHandleNullComparisons() {
        MetricResult result = MetricResult.builder()
                .name("Test")
                .score(50)
                .weight(0.5)
                .build();

        assertThat(result.equals(null)).isFalse();
        assertThat(result.equals("not a result")).isFalse();
        assertThat(result.equals(result)).isTrue();
    }

    @Test
    void commitInfoShouldHandleNullComparisons() {
        CommitInfo commit = CommitInfo.builder()
                .sha("abc")
                .build();

        assertThat(commit.equals(null)).isFalse();
        assertThat(commit.equals("not a commit")).isFalse();
        assertThat(commit.equals(commit)).isTrue();
    }

    @Test
    void maintainabilityReportShouldHandleNullComparisons() {
        MaintainabilityReport report = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(80)
                .metrics(new HashMap<>())
                .build();

        assertThat(report.equals(null)).isFalse();
        assertThat(report.equals("not a report")).isFalse();
        assertThat(report.equals(report)).isTrue();
    }

    @Test
    void repositoryInfoShouldHandleDifferentClassComparison() {
        RepositoryInfo repo1 = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .build();

        Object differentObject = new Object();

        assertThat(repo1.equals(differentObject)).isFalse();
    }

    @Test
    void metricResultShouldHandleDifferentValues() {
        MetricResult result1 = MetricResult.builder()
                .name("Test")
                .score(50)
                .weight(0.5)
                .build();

        MetricResult result2 = MetricResult.builder()
                .name("Test2")
                .score(50)
                .weight(0.5)
                .build();

        MetricResult result3 = MetricResult.builder()
                .name("Test")
                .score(60)
                .weight(0.5)
                .build();

        MetricResult result4 = MetricResult.builder()
                .name("Test")
                .score(50)
                .weight(0.6)
                .build();

        assertThat(result1.equals(result2)).isFalse();
        assertThat(result1.equals(result3)).isFalse();
        assertThat(result1.equals(result4)).isFalse();
    }

    @Test
    void maintainabilityReportShouldHandleDifferentScores() {
        MaintainabilityReport report1 = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(80.0)
                .metrics(new HashMap<>())
                .build();

        MaintainabilityReport report2 = MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(85.0)
                .metrics(new HashMap<>())
                .build();

        assertThat(report1.equals(report2)).isFalse();
        assertThat(report1.hashCode()).isNotEqualTo(report2.hashCode());
    }

    @Test
    void maintainabilityReportShouldHandleDifferentRepoNames() {
        MaintainabilityReport report1 = MaintainabilityReport.builder()
                .repositoryFullName("owner1/repo")
                .overallScore(80.0)
                .metrics(new HashMap<>())
                .build();

        MaintainabilityReport report2 = MaintainabilityReport.builder()
                .repositoryFullName("owner2/repo")
                .overallScore(80.0)
                .metrics(new HashMap<>())
                .build();

        assertThat(report1.equals(report2)).isFalse();
    }

    @Test
    void metricResultShouldValidateScoreBoundariesExactly() {
        assertThatThrownBy(() -> MetricResult.builder()
                .name("Test")
                .score(-0.01)
                .weight(0.5)
                .build())
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> MetricResult.builder()
                .name("Test")
                .score(100.01)
                .weight(0.5)
                .build())
                .isInstanceOf(IllegalArgumentException.class);

        MetricResult min = MetricResult.builder()
                .name("Test")
                .score(0)
                .weight(0.5)
                .build();
        assertThat(min.getScore()).isEqualTo(0);

        MetricResult max = MetricResult.builder()
                .name("Test")
                .score(100)
                .weight(0.5)
                .build();
        assertThat(max.getScore()).isEqualTo(100);
    }

    @Test
    void metricResultShouldValidateWeightBoundariesExactly() {
        assertThatThrownBy(() -> MetricResult.builder()
                .name("Test")
                .score(50)
                .weight(-0.01)
                .build())
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> MetricResult.builder()
                .name("Test")
                .score(50)
                .weight(1.01)
                .build())
                .isInstanceOf(IllegalArgumentException.class);

        MetricResult min = MetricResult.builder()
                .name("Test")
                .score(50)
                .weight(0)
                .build();
        assertThat(min.getWeight()).isEqualTo(0);

        MetricResult max = MetricResult.builder()
                .name("Test")
                .score(50)
                .weight(1)
                .build();
        assertThat(max.getWeight()).isEqualTo(1);
    }

    @Test
    void maintainabilityReportShouldHandleAllRatingBoundaries() {
        assertThat(createReportWithScore(100).getRating()).isEqualTo("EXCELLENT");
        assertThat(createReportWithScore(90).getRating()).isEqualTo("EXCELLENT");
        assertThat(createReportWithScore(89.99).getRating()).isEqualTo("GOOD");
        assertThat(createReportWithScore(75).getRating()).isEqualTo("GOOD");
        assertThat(createReportWithScore(74.99).getRating()).isEqualTo("FAIR");
        assertThat(createReportWithScore(60).getRating()).isEqualTo("FAIR");
        assertThat(createReportWithScore(59.99).getRating()).isEqualTo("POOR");
        assertThat(createReportWithScore(40).getRating()).isEqualTo("POOR");
        assertThat(createReportWithScore(39.99).getRating()).isEqualTo("CRITICAL");
        assertThat(createReportWithScore(0).getRating()).isEqualTo("CRITICAL");
    }

    private MaintainabilityReport createReportWithScore(double score) {
        return MaintainabilityReport.builder()
                .repositoryFullName("owner/repo")
                .overallScore(score)
                .metrics(new HashMap<>())
                .build();
    }

    @Test
    void commitInfoShouldHandleDifferentSha() {
        CommitInfo commit1 = CommitInfo.builder()
                .sha("abc")
                .build();

        CommitInfo commit2 = CommitInfo.builder()
                .sha("def")
                .build();

        assertThat(commit1.equals(commit2)).isFalse();
        assertThat(commit1.hashCode()).isNotEqualTo(commit2.hashCode());
    }

    @Test
    void repositoryInfoShouldHandleDifferentNames() {
        RepositoryInfo repo1 = RepositoryInfo.builder()
                .owner("owner")
                .name("repo1")
                .build();

        RepositoryInfo repo2 = RepositoryInfo.builder()
                .owner("owner")
                .name("repo2")
                .build();

        assertThat(repo1.equals(repo2)).isFalse();
    }

    @Test
    void metricResultShouldCalculateWeightedScoreCorrectly() {
        MetricResult result = MetricResult.builder()
                .name("Test")
                .score(80)
                .weight(0.25)
                .build();

        assertThat(result.getWeightedScore()).isEqualTo(20.0);
    }

    @Test
    void commitInfoShouldAcceptNullOptionalFields() {
        CommitInfo commit = CommitInfo.builder()
                .sha("abc")
                .message(null)
                .author(null)
                .date(null)
                .build();

        assertThat(commit.getSha()).isEqualTo("abc");
        assertThat(commit.getMessage()).isNull();
        assertThat(commit.getAuthor()).isNull();
        assertThat(commit.getDate()).isNull();
    }
}
