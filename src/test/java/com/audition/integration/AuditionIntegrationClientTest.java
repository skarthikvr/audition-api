package com.audition.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@Getter
@Setter
class AuditionIntegrationClientTest {

    private AuditionIntegrationClient client;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        client = new AuditionIntegrationClient(restTemplate);
        // set baseUrl
        org.springframework.test.util.ReflectionTestUtils.setField(client, "baseUrl", "https://example.com/");
    }

    @Test
    void testPostsSuccessReturnsBody() {
        final var posts = List.of(new AuditionPost());
        final ResponseEntity<List<AuditionPost>> response = ResponseEntity.ok(posts);
        when(restTemplate.exchange(eq("https://example.com/posts"), eq(HttpMethod.GET), any(),
            any(ParameterizedTypeReference.class)))
            .thenReturn(response);

        final var result = client.getPosts();

        assertThat(result).isSameAs(posts);
    }

    @Test
    void testPostsNotFoundThrowsSystemException() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
            any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        assertThatThrownBy(() -> client.getPosts())
            .isInstanceOf(SystemException.class)
            .hasMessageContaining("Cannot find any Posts");
    }

    @Test
    void testPostByIdSuccessReturnsBody() {
        final var post = new AuditionPost();
        final ResponseEntity<AuditionPost> response = ResponseEntity.ok(post);
        when(restTemplate.exchange(eq("https://example.com/posts/1"), eq(HttpMethod.GET), any(),
            any(ParameterizedTypeReference.class)))
            .thenReturn(response);

        final var result = client.getPostById("1");

        assertThat(result).isSameAs(post);
    }

    @Test
    void testPostByIdNotFoundThrowsSystemExceptionWithId() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
            any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        assertThatThrownBy(() -> client.getPostById("123"))
            .isInstanceOf(SystemException.class)
            .hasMessageContaining("Cannot find a Post with id: 123");
    }

    @Test
    void testCommentsByPostIdSuccessReturnsBody() {
        final var comments = List.of(new Comment());
        final ResponseEntity<List<Comment>> response = ResponseEntity.ok(comments);
        when(restTemplate.exchange(eq("https://example.com/posts/1/comments"), eq(HttpMethod.GET), any(),
            any(ParameterizedTypeReference.class)))
            .thenReturn(response);

        final var result = client.getCommentsByPostId("1");

        assertThat(result).isSameAs(comments);
    }

    @Test
    void testCommentsByPostIdNotFoundThrowsSystemException() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
            any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        assertThatThrownBy(() -> client.getCommentsByPostId("55"))
            .isInstanceOf(SystemException.class)
            .hasMessageContaining("Cannot find Comments with post id: 55");
    }

    @Test
    void testCommentsForPostSuccessBuildsCorrectUrlAndReturnsBody() {
        final var comments = List.of(new Comment());
        final ResponseEntity<List<Comment>> response = ResponseEntity.ok(comments);
        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        when(restTemplate.exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(),
            any(ParameterizedTypeReference.class)))
            .thenReturn(response);

        final var result = client.getCommentsForPost("2");

        assertThat(result).isSameAs(comments);
        final String usedUrl = urlCaptor.getValue();
        assertThat(usedUrl).contains("comments").contains("postId=2");
    }

    @Test
    void testCommentsForPostServerErrorThrowsSystemException() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
            any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error"));

        assertThatThrownBy(() -> client.getCommentsForPost("3"))
            .isInstanceOf(SystemException.class)
            .hasMessageContaining("Server Error");
    }
}
