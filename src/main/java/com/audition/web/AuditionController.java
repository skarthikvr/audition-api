package com.audition.web;

import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.service.AuditionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@Getter
@Setter
public class AuditionController {

    private final AuditionService auditionService;

    /**
     * Controller constructor.
     *
     * @param auditionService the service used to fetch posts and comments (injected)
     */
    public AuditionController(final AuditionService auditionService) {
        this.auditionService = auditionService;
    }

    @Tag(name = "Get Audition Posts")
    @Operation(description = "Fetch all audition posts or for a particular user id")
    @RequestMapping(value = "/posts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<AuditionPost> getPosts(@RequestParam(required = false) @Positive final Integer userId) {

        if (userId != null) {
            return auditionService.getPosts()
                .stream()
                .filter(ap -> ap.getUserId() == userId)
                .toList();
        } else {
            return auditionService.getPosts();
        }
    }

    /**
     * Fetch a single AuditionPost by id.
     *
     * <p>The id path variable is validated by a regex to ensure it is a positive numeric value. The
     * controller delegates to {@link AuditionService#getPostById(String)} and returns the result as the response body.
     * If no post is found the service may return {@code null} which will yield a 200 response with an empty body unless
     * an exception is thrown by the service layer.
     *
     * @param postId the post identifier (numeric string, validated)
     * @return the matching {@link AuditionPost} or {@code null} when not present
     */
    @Tag(name = "Get Audition Posts")
    @Operation(description = "Fetch all audition posts for a particular Audition Post id")
    @RequestMapping(value = "/posts/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody AuditionPost getPosts(
        @Valid @Pattern(regexp = "[1-9]\\d*", message = "Audition Post Id should be a number") @PathVariable("id") final String postId) {
        return auditionService.getPostById(postId);

    }

    // DONE Add additional methods to return comments for each post. Hint: Check https://jsonplaceholder.typicode.com/

    /**
     * Fetch comments for a post using the nested resource endpoint <code>/posts/{postId}/comments</code>.
     *
     * <p>This endpoint accepts the post id as a path variable (validated). It delegates
     * to {@link AuditionService#getCommentsByPostId(String)} and returns the service response.
     *
     * @param postId the post identifier (numeric string, validated)
     * @return a list of {@link Comment} or {@code null} when the upstream returns no body
     */
    @Tag(name = "Get Comments")
    @Operation(description = "Fetch all comments for a particular Audition Post id")
    @RequestMapping(value = "/posts/{id}/comments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Comment> getComments(
        @Valid @Pattern(regexp = "[1-9]\\d*", message = "Audition Post Id should be a number") @PathVariable("id") final String postId) {
        return auditionService.getCommentsByPostId(postId);
    }

    /**
     * Fetch comments for a post using the query endpoint <code>/comments?postId={postId}</code>.
     *
     * <p>This endpoint accepts the post id as a required request parameter (validated). It delegates
     * to {@link AuditionService#getCommentsForPost(String)} and returns the service response.
     *
     * @param postId the post identifier (numeric string, validated)
     * @return a list of {@link Comment} or {@code null} when the upstream returns no body
     */
    @Tag(name = "Get Comments")
    @Operation(description = "Fetch all comments for a particular Audition Post id passing Request Parameters")
    @RequestMapping(value = "/comments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Comment> getCommentsForPost(
        @Valid @Pattern(regexp = "[1-9]\\d*", message = "Audition Post Id should be a number") @RequestParam(required = true) final String postId) {
        return auditionService.getCommentsForPost(postId);
    }
}
