package com.audition.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@Getter
@Setter
class AuditionIntegrationClientNullBodyTest {

    private AuditionIntegrationClient client;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        client = new AuditionIntegrationClient(restTemplate);
        org.springframework.test.util.ReflectionTestUtils.setField(client, "baseUrl", "https://example.com/");
    }

    @Test
    void getPostByIdReturnsNullWhenBodyNull() {
        final ResponseEntity<AuditionPost> response = ResponseEntity.ok(null);
        when(restTemplate.exchange(eq("https://example.com/posts/1"), any(HttpMethod.class), any(),
            any(ParameterizedTypeReference.class)))
            .thenReturn(response);

        final var result = client.getPostById("1");
        assertThat(result).isNull();
    }

    @Test
    void getCommentsByPostIdReturnsNullWhenBodyNull() {
        final ResponseEntity<List<Comment>> response = ResponseEntity.ok(null);
        when(restTemplate.exchange(eq("https://example.com/posts/1/comments"), any(HttpMethod.class), any(),
            any(ParameterizedTypeReference.class)))
            .thenReturn(response);

        final var result = client.getCommentsByPostId("1");
        assertThat(result).isNull();
    }
}

