package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service layer facade for Audition-related operations.
 *
 * <p>This simple service delegates calls to the {@link AuditionIntegrationClient} which
 * is responsible for making HTTP calls to the upstream posts/comments API. The service exists to provide a stable,
 * testable adapter for controllers and other application components and to centralise any future business logic that
 * should be applied to audition data.
 */
@Service
@Getter
@Setter
public class AuditionService {

    private final AuditionIntegrationClient auditionIntegrationClient;

    @Autowired
    public AuditionService(final AuditionIntegrationClient auditionIntegrationClient) {
        this.auditionIntegrationClient = auditionIntegrationClient;
    }

    /**
     * Retrieve all posts from the upstream integration client.
     *
     * @return a list of {@link AuditionPost}; may be {@code null} when the upstream returns no body
     */
    public List<AuditionPost> getPosts() {
        return auditionIntegrationClient.getPosts();
    }

    /**
     * Retrieve a single post by identifier.
     *
     * @param postId the id of the post to fetch
     * @return the matching {@link AuditionPost} or {@code null} when no content was returned
     */
    public AuditionPost getPostById(final String postId) {
        return auditionIntegrationClient.getPostById(postId);
    }

    /**
     * Retrieve comments that belong to the specified post using the /posts/{postId}/comments endpoint.
     *
     * @param postId the post identifier
     * @return a list of {@link Comment} or {@code null} when no content was returned by the upstream service
     */
    public List<Comment> getCommentsByPostId(final String postId) {
        return auditionIntegrationClient.getCommentsByPostId(postId);
    }

    /**
     * Retrieve comments for a post using the query endpoint (/comments?postId={postId}).
     *
     * <p>This method delegates to the integration client which determines which upstream
     * endpoint to use. The difference between this method and {@link #getCommentsByPostId(String)} is the upstream
     * route used (query vs nested resource) — both return equivalent data.
     *
     * @param postId the post identifier
     * @return a list of {@link Comment} or {@code null} when the upstream returns no body
     */
    public List<Comment> getCommentsForPost(final String postId) {
        return auditionIntegrationClient.getCommentsForPost(postId);
    }
}
