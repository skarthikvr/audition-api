package com.audition.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.service.AuditionService;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Getter
@Setter
class AuditionControllerTest {

    private AuditionController controller;

    @Mock
    private AuditionService mockService;

    @BeforeEach
    void setUp() {
        controller = new AuditionController(mockService);
    }

    @Test
    void getPostsWithoutUserIdReturnsAll() {
        final var posts = List.of(new AuditionPost(), new AuditionPost());
        when(mockService.getPosts()).thenReturn(posts);

        final var result = controller.getPosts((Integer) null);

        assertEquals(posts, result);
    }

    @Test
    void getPostsWithUserIdFiltersByUserId() {
        final var p1 = new AuditionPost();
        p1.setUserId(1);
        final var p2 = new AuditionPost();
        p2.setUserId(2);
        when(mockService.getPosts()).thenReturn(List.of(p1, p2));

        final var result = controller.getPosts(1);

        assertThat(result).hasSize(1);
        assertEquals(1, result.get(0).getUserId());
    }

    @Test
    void getPostByIdDelegates() {
        final var p = new AuditionPost();
        when(mockService.getPostById("5")).thenReturn(p);

        final var result = controller.getPosts("5");

        assertEquals(p, result);
    }

    @Test
    void getCommentsDelegates() {
        final var comments = List.of(new Comment());
        when(mockService.getCommentsByPostId("1")).thenReturn(comments);

        final var result = controller.getComments("1");

        assertEquals(comments, result);
    }

    @Test
    void getCommentsForPostDelegates() {
        final var comments = List.of(new Comment());
        when(mockService.getCommentsForPost("1")).thenReturn(comments);

        final var result = controller.getCommentsForPost("1");

        assertEquals(comments, result);
    }
}
