package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Integration client responsible for calling the external JSONPlaceholder-like API to fetch posts and comments. The
 * class encapsulates RestTemplate usage and translates HTTP errors into {@link SystemException} instances so the rest
 * of the application can handle errors in a consistent manner.
 *
 * <p>Provided methods:
 * <ul>
 *   <li>{@link #getPosts()} - GET /posts returning a list of {@link AuditionPost}
 *   <li>{@link #getPostById(String)} - GET /posts/{id} returning a single {@link AuditionPost}
 *   <li>{@link #getCommentsByPostId(String)} - GET /posts/{postId}/comments returning a list of {@link Comment}
 *   <li>{@link #getCommentsForPost(String)} - GET /comments?postId={postId} returning a list of {@link Comment}
 * </ul>
 *
 * <p>Errors from the downstream service are converted to {@link SystemException} with an
 * appropriate title and numeric status code. Client (4xx) and server (5xx) errors are
 * handled separately by private helpers.
 */
@Component
@SuppressFBWarnings("EI_EXPOSE_REP2")
@Getter
@Setter
public class AuditionIntegrationClient {

    private static final String URL_SEPARATOR = "/";
    private static final String NO_POSTS_FOUND = "Cannot find any Posts";
    private static final String NO_POST_FOUND = "Cannot find a Post with id: ";
    private static final String NO_COMMENTS_FOR_POSTS = "Cannot find Comments with post id: ";
    private final RestTemplate restTemplate;
    @Value("${api.host.baseurl}")
    private String baseUrl;

    public AuditionIntegrationClient(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetch all posts from the upstream API.
     *
     * @return a non-null list of {@link AuditionPost} or null if the upstream returned no body
     * @throws SystemException when the upstream responds with a client (4xx) or server (5xx) error
     */
    public List<AuditionPost> getPosts() {
        ResponseEntity<List<AuditionPost>> response = null; //NOPMD
        try {
            response = restTemplate.exchange(
                baseUrl.concat("posts"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
            );
        } catch (final HttpClientErrorException e) {
            handleClientError(e, NO_POSTS_FOUND);
        } catch (final HttpServerErrorException e) {
            handleServerError(e);
        }
        return response.getBody();
    }

    /**
     * Fetch a single post by id.
     *
     * @param id the post id
     * @return the {@link AuditionPost} returned by the upstream service or null when the response body is empty
     * @throws SystemException when the upstream responds with a client (4xx) or server (5xx) error
     */
    public AuditionPost getPostById(final String id) {
        ResponseEntity<AuditionPost> response = null; //NOPMD
        try {
            response = restTemplate.exchange(
                baseUrl.concat("posts").concat(URL_SEPARATOR).concat(id),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
            );

        } catch (final HttpClientErrorException e) {
            handleClientError(e, NO_POST_FOUND + id);
        } catch (final HttpServerErrorException e) {
            handleServerError(e);
        }
        return response.getBody();
    }

    /**
     * Fetch comments for a post using the /posts/{postId}/comments endpoint.
     *
     * @param postId the post identifier
     * @return a list of {@link Comment} or null when the upstream returned an empty body
     */
    public List<Comment> getCommentsByPostId(final String postId) {
        ResponseEntity<List<Comment>> response = null; //NOPMD
        try {
            response = restTemplate.exchange(
                baseUrl.concat("posts").concat(URL_SEPARATOR).concat(postId).concat(URL_SEPARATOR).concat("comments"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
            );
        } catch (final HttpClientErrorException e) {
            handleClientError(e, NO_COMMENTS_FOR_POSTS + postId);
        } catch (final HttpServerErrorException e) {
            handleServerError(e);
        }
        return response.getBody();
    }

    /**
     * Fetch comments using the query endpoint /comments?postId={postId}.
     *
     * @param postId the post id
     * @return the comments for the post or null when the upstream response body is empty
     */
    public List<Comment> getCommentsForPost(final String postId) {
        // The response from the upstream service
        ResponseEntity<List<Comment>> response = null;//NOPMD
        try {
            final String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("comments")
                .queryParam("postId", postId)
                .toUriString();

            response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
            );
        } catch (final HttpClientErrorException e) {
            handleClientError(e, NO_COMMENTS_FOR_POSTS + postId);
        } catch (final HttpServerErrorException e) {
            handleServerError(e);
        }
        return response.getBody();
    }

    /**
     * Convert downstream client errors to {@link SystemException} and include a friendly detail message. If the error
     * is a NOT_FOUND (404) the provided detail will be used as the exception detail. Otherwise the exception message
     * and the downstream HTTP status metadata are included.
     *
     * @param e      the downstream HTTP exception
     * @param detail friendly detail message to be used for NOT_FOUND errors
     */
    private void handleClientError(final HttpStatusCodeException e, final String detail) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new SystemException(detail, HttpStatus.valueOf(e.getStatusCode().value()).getReasonPhrase(),
                e.getStatusCode().value());
        } else {
            throw new SystemException(e.getMessage(), HttpStatus.valueOf(e.getStatusCode().value()).getReasonPhrase(),
                e.getStatusCode().value(), e);
        }
    }

    /**
     * Convert downstream server errors (5xx) into {@link SystemException} preserving the original exception as the
     * cause.
     *
     * @param e the server-side HTTP exception
     */
    private void handleServerError(final HttpStatusCodeException e) {
        throw new SystemException(e.getMessage(), HttpStatus.valueOf(e.getStatusCode().value()).getReasonPhrase(),
            e.getStatusCode().value(), e);
    }
}
