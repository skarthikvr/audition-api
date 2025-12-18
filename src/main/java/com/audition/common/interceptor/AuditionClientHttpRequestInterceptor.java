package com.audition.common.interceptor;

import com.audition.common.logging.AuditionLogger;
import java.io.IOException;
import java.nio.charset.Charset;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Interceptor for Spring's {@link RestTemplate} that logs request and response details using an
 * {@link AuditionLogger}.
 */
@Setter
@Getter
public class AuditionClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AuditionClientHttpRequestInterceptor.class);

    private final AuditionLogger auditLogger;

    /**
     * Convenience constructor that accepts an {@link AuditionLogger}. Useful for unit tests.
     *
     * @param auditLogger the audit logger instance to use for logging
     */
    public AuditionClientHttpRequestInterceptor(final AuditionLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    /**
     * Intercept the given HTTP request, execute it and then log request and response details.
     *
     * @param request   the HTTP request
     * @param body      the request body as a byte array
     * @param execution the request execution callback used to actually execute the request
     * @return the {@link ClientHttpResponse} returned from the execution
     * @throws IOException in case of I/O errors when executing or logging the response
     */
    @Override
    public ClientHttpResponse intercept(
        final HttpRequest request, final byte[] body,
        final ClientHttpRequestExecution execution) throws IOException {
        logRequestDetails(request);
        final ClientHttpResponse response = execution.execute(request, body);
        logResponseDetails(response);
        return response;
    }

    /**
     * Log relevant request fields using the configured {@link AuditionLogger}.
     *
     * <p>Logged fields:
     * <ul>
     *   <li>Request headers, method and URI
     * </ul>
     *
     * @param request the original request
     * @throws IOException when reading the response body fails
     */
    private void logRequestDetails(final HttpRequest request) throws IOException {
        if (LOGGER.isInfoEnabled()) {
            auditLogger.info(LOGGER, "Request Headers: {}", request.getHeaders());
            auditLogger.info(LOGGER, "Request Method: {}", request.getMethod());
            auditLogger.info(LOGGER, "Request URI: {}", request.getURI());
        }
    }

    /**
     * Log relevant response fields using the configured {@link AuditionLogger}.
     *
     * <p>Logged fields:
     * <ul>
     *   <li>Response headers, status code and body
     * </ul>
     *
     * @param response the HTTP response
     * @throws IOException when reading the response body fails
     */
    private void logResponseDetails(final ClientHttpResponse response) throws IOException {
        if (LOGGER.isInfoEnabled()) {
            auditLogger.info(LOGGER, "Response Headers: {}", response.getHeaders());
            auditLogger.info(LOGGER, "Headers: {}", response.getHeaders());
            auditLogger.info(LOGGER, "Response Status Code: {}", response.getStatusCode());
            auditLogger.info(LOGGER, "Response: {}",
                StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
        }
    }

}