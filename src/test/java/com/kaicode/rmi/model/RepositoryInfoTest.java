package com.kaicode.rmi.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepositoryInfoTest {

    @Test
    void shouldBuildRepositoryInfoWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        
        RepositoryInfo repo = RepositoryInfo.builder()
                .owner("testowner")
                .name("testrepo")
                .description("Test description")
                .stars(100)
                .forks(20)
                .openIssues(5)
                .lastUpdated(now)
                .hasWiki(true)
                .hasIssues(true)
                .defaultBranch("main")
                .size(1024)
                .build();

        assertThat(repo.getOwner()).isEqualTo("testowner");
        assertThat(repo.getName()).isEqualTo("testrepo");
        assertThat(repo.getDescription()).isEqualTo("Test description");
        assertThat(repo.getStars()).isEqualTo(100);
        assertThat(repo.getForks()).isEqualTo(20);
        assertThat(repo.getOpenIssues()).isEqualTo(5);
        assertThat(repo.getLastUpdated()).isEqualTo(now);
        assertThat(repo.hasWiki()).isTrue();
        assertThat(repo.hasIssues()).isTrue();
        assertThat(repo.getDefaultBranch()).isEqualTo("main");
        assertThat(repo.getSize()).isEqualTo(1024);
    }

    @Test
    void shouldThrowExceptionWhenOwnerIsNull() {
        assertThatThrownBy(() -> RepositoryInfo.builder()
                .name("testrepo")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("owner");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> RepositoryInfo.builder()
                .owner("testowner")
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("name");
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        RepositoryInfo repo1 = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .build();

        RepositoryInfo repo2 = RepositoryInfo.builder()
                .owner("owner")
                .name("repo")
                .stars(100)
                .build();

        assertThat(repo1).isEqualTo(repo2);
        assertThat(repo1.hashCode()).isEqualTo(repo2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentOwnerOrName() {
        RepositoryInfo repo1 = RepositoryInfo.builder()
                .owner("owner1")
                .name("repo")
                .build();

        RepositoryInfo repo2 = RepositoryInfo.builder()
                .owner("owner2")
                .name("repo")
                .build();

        assertThat(repo1).isNotEqualTo(repo2);
    }
}
