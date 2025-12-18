package com.audition.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.audition.model.AuditionPost;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@Getter
@Setter
class AuditionIntegrationClientAdditionalTest {

    private AuditionIntegrationClient client;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        client = new AuditionIntegrationClient(restTemplate);
        org.springframework.test.util.ReflectionTestUtils.setField(client, "baseUrl", "https://example.com/");
    }

    @Test
    void testPostsBadRequestThrowsSystemExceptionWithMessage() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(),
            any(ParameterizedTypeReference.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad"));

        assertThatThrownBy(() -> client.getPosts()).hasMessageContaining("Bad");
    }

    @Test
    void testPostById_nullResponseBody_returnsNull() {
        final ResponseEntity<AuditionPost> response = ResponseEntity.ok(null);
        when(restTemplate.exchange(eq("https://example.com/posts/1"), eq(HttpMethod.GET), any(),
            any(ParameterizedTypeReference.class)))
            .thenReturn(response);

        final var result = client.getPostById("1");
        assertThat(result).isNull();
    }
}
