package com.audition.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@Getter
@Setter
class AuditionServiceTest {

    private AuditionService auditionService;

    @Mock
    private AuditionIntegrationClient mockClient;

    @BeforeEach
    void setUp() {
        auditionService = new AuditionService(mockClient);
        ReflectionTestUtils.setField(auditionService, "auditionIntegrationClient", mockClient);
    }

    @Test
    void testPostsDelegatesToIntegrationClient() {
        final var posts = List.of(new AuditionPost());
        when(mockClient.getPosts()).thenReturn(posts);

        final var result = auditionService.getPosts();

        assertThat(result).isSameAs(posts);
        verify(mockClient).getPosts();
    }

    @Test
    void testPostByIdDelegatesToIntegrationClient() {
        final var post = new AuditionPost();
        when(mockClient.getPostById("1")).thenReturn(post);

        final var result = auditionService.getPostById("1");

        assertThat(result).isSameAs(post);
        verify(mockClient).getPostById("1");
    }

    @Test
    void testCommentsByPostIdDelegatesToIntegrationClient() {
        final var comments = List.of(new Comment());
        when(mockClient.getCommentsByPostId("1")).thenReturn(comments);

        final var result = auditionService.getCommentsByPostId("1");

        assertThat(result).isSameAs(comments);
        verify(mockClient).getCommentsByPostId("1");
    }

    @Test
    void testCommentsForPostDelegatesToIntegrationClient() {
        final var comments = List.of(new Comment());
        when(mockClient.getCommentsForPost("1")).thenReturn(comments);

        final var result = auditionService.getCommentsForPost("1");

        assertThat(result).isSameAs(comments);
        verify(mockClient).getCommentsForPost("1");
    }
}
