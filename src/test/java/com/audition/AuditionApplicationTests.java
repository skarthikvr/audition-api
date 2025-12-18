package com.audition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.audition.configuration.WebServiceConfiguration;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "api.host.baseurl=https://jsonplaceholder.typicode.com")
@Getter
@Setter
class AuditionApplicationTests {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    // DONE implement unit test. Note that an applicant should create additional unit tests as required.
    @Autowired
    WebServiceConfiguration webServiceConfiguration;
    @Autowired
    private TestRestTemplate testRestTemplate;
    private MockRestServiceServer mockServer;

    @Test
    void contextLoads() {
    }

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(testRestTemplate.getRestTemplate());
    }

    @Test
    void testPostsReturnsPostsFromIntegrationClient() {
        final String postsJson = "[ { \"userId\": 1, \"id\": 10, \"title\": \"T1\", \"body\": \"B1\" },"
            + " { \"userId\": 2, \"id\": 11, \"title\": \"T2\", \"body\": \"B2\" } ]";
        mockServer.expect(requestTo(BASE_URL + "/posts"))
            .andRespond(withSuccess(postsJson, MediaType.APPLICATION_JSON));

        final ResponseEntity<AuditionPost[]> resp = testRestTemplate.getForEntity(
            BASE_URL + "/posts", AuditionPost[].class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        final AuditionPost[] body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(2);
        assertThat(body[0].getId()).isEqualTo(10);
        mockServer.verify();
    }

    @Test
    void testPostByIdReturnsPost() {
        final String postJson = "{ \"userId\": 1, \"id\": 1, \"title\": \"Single\", \"body\": \"Body\" }";
        mockServer.expect(requestTo(BASE_URL + "/posts/1"))
            .andRespond(withSuccess(postJson, MediaType.APPLICATION_JSON));

        final ResponseEntity<AuditionPost> resp = testRestTemplate.getForEntity(BASE_URL + "/posts/1",
            AuditionPost.class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        final AuditionPost body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo(1);
        assertThat(body.getTitle()).isEqualTo("Single");
        mockServer.verify();
    }

    @Test
    void testCommentsForPostQueryEndpointReturnsComments() {
        final String commentsJson = "[ { \"postId\": 1, \"id\": 101, \"name\": \"C1\", \"body\": \"cb1\", \"email\": \"e@e.com\" } ]";
        mockServer.expect(requestTo(BASE_URL + "/comments?postId=1"))
            .andRespond(withSuccess(commentsJson, MediaType.APPLICATION_JSON));

        final ResponseEntity<Comment[]> resp = testRestTemplate.getForEntity(BASE_URL + "/comments?postId=1",
            Comment[].class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        final Comment[] body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(1);
        assertThat(body[0].getPostId()).isEqualTo(1);
        mockServer.verify();
    }

    @Test
    void testPostByIdDownstreamNotFoundMapsTo404() {
        mockServer.expect(requestTo(BASE_URL + "/posts/123"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND).body("Not Found").contentType(MediaType.TEXT_PLAIN));

        final ResponseEntity<String> resp = testRestTemplate.getForEntity(BASE_URL + "/posts/123", String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).contains("Not Found");
        mockServer.verify();
    }

    @Test
    void testCommentsForPostDownstreamServerErrorMapsTo500() {
        mockServer.expect(requestTo(BASE_URL + "/comments?postId=9"))
            .andRespond(withServerError().contentType(MediaType.TEXT_PLAIN).body("Server Error"));

        final ResponseEntity<String> resp = testRestTemplate.getForEntity(BASE_URL + "/comments?postId=9",
            String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody()).contains("Server Error");
        mockServer.verify();
    }

    @Test
    void testPostsWithUserIdFiltersEndToEnd() {
        final String postsJson = "[ { \"userId\": 1, \"id\": 10, \"title\": \"T1\", \"body\": \"B1\" },"
            + " { \"userId\": 2, \"id\": 11, \"title\": \"T2\", \"body\": \"B2\" } ]";
        mockServer.expect(requestTo(BASE_URL + "/posts?userId=1"))
            .andRespond(withStatus(HttpStatus.OK).body(postsJson).contentType(MediaType.APPLICATION_JSON));

        final ResponseEntity<AuditionPost[]> resp = testRestTemplate.getForEntity(BASE_URL + "/posts?userId=1",
            AuditionPost[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        final AuditionPost[] body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(2);
        assertThat(body[0].getUserId()).isEqualTo(1);
        mockServer.verify();
    }

}
