package com.kaicode.rmi.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommitInfoTest {

    @Test
    void shouldBuildCommitInfoWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        
        CommitInfo commit = CommitInfo.builder()
                .sha("abc123")
                .message("Test commit")
                .author("John Doe")
                .date(now)
                .build();

        assertThat(commit.getSha()).isEqualTo("abc123");
        assertThat(commit.getMessage()).isEqualTo("Test commit");
        assertThat(commit.getAuthor()).isEqualTo("John Doe");
        assertThat(commit.getDate()).isEqualTo(now);
    }

    @Test
    void shouldThrowExceptionWhenShaIsNull() {
        assertThatThrownBy(() -> CommitInfo.builder()
                .message("Test")
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldImplementEqualsAndHashCodeBasedOnSha() {
        CommitInfo commit1 = CommitInfo.builder()
                .sha("abc123")
                .message("Message 1")
                .build();

        CommitInfo commit2 = CommitInfo.builder()
                .sha("abc123")
                .message("Message 2")
                .build();

        assertThat(commit1).isEqualTo(commit2);
        assertThat(commit1.hashCode()).isEqualTo(commit2.hashCode());
    }
}
