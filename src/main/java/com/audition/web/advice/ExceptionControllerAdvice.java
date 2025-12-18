package com.audition.web.advice;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


/**
 * Global controller advice that converts exceptions thrown by controllers into standardized {@link ProblemDetail}
 * payloads.
 *
 * <p>This advice provides a small set of handlers for common error types used by the
 * application:
 * <ul>
 *   <li>{@link ConstraintViolationException} — validation failures are mapped to HTTP 400
 *   <li>{@link SystemException} — application-level exceptions that contain a title and numeric status
 *   <li>Generic {@link Exception} — mapped to an internal server error unless the underlying
 *       exception provides a clearer HTTP status (for example {@link HttpClientErrorException})
 * </ul>
 *
 * <p>Behavior notes:
 * <ul>
 *   <li>All handlers return a {@link ProblemDetail} object with a status, title and detail.
 *   <li>SystemException preserves the configured title and numeric status when possible; if the
 *       status is invalid the handler falls back to HTTP 500 and logs the mapping issue.
 *   <li>getMessageFromException() ensures the ProblemDetail.detail is populated with a useful
 *       message and falls back to a safe default when the exception message is blank.
 * </ul>
 *
 * <p>Usage: the class is annotated with {@link ControllerAdvice} and is picked up by Spring's
 * component scanning; no further wiring is necessary.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    public static final String DEFAULT_TITLE = "API Error Occurred";
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionControllerAdvice.class);
    private static final String ERROR_MESSAGE = " Error Code from Exception could not be mapped to a valid HttpStatus Code - ";
    private static final String DEFAULT_MESSAGE = "API Error occurred. Please contact support or administrator.";

    private final AuditionLogger logger;

    /**
     * Handle validation failures coming from JSR-303 / jakarta.validation.
     *
     * <p>Constraint violations are considered client errors and are mapped to HTTP 400
     * with the ProblemDetail.title set to the standard reason phrase for BAD_REQUEST.
     *
     * @param e the constraint violation exception
     * @return a ProblemDetail representing a 400 Bad Request
     */
    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleMainException(final ConstraintViolationException e) {
        return createProblemDetail(e, HttpStatusCode.valueOf(HttpStatus.BAD_REQUEST.value()));

    }

    /**
     * Generic exception handler that attempts to map known exception types to an HTTP status. If the exception cannot
     * be mapped it returns a ProblemDetail with HTTP 500.
     *
     * @param e the thrown exception
     * @return a ProblemDetail with an appropriate status and message
     */
    @ExceptionHandler(Exception.class)
    ProblemDetail handleMainException(final Exception e) {
        final HttpStatusCode status = getHttpStatusCodeFromException(e);
        return createProblemDetail(e, status);

    }

    /**
     * Handle application-level {@link SystemException} which carry a title and numeric status. The handler attempts to
     * convert the configured status to an {@link HttpStatusCode}. If conversion fails the handler logs the issue and
     * returns HTTP 500.
     *
     * @param e the application-level SystemException
     * @return a ProblemDetail reflecting the SystemException's title and mapped status
     */
    @ExceptionHandler(SystemException.class)
    ProblemDetail handleSystemException(final SystemException e) {
        final HttpStatusCode status = getHttpStatusCodeFromSystemException(e);
        return createProblemDetail(e, status);

    }

    /**
     * Build a ProblemDetail for the supplied exception and status code. The method fills the detail from the exception
     * message (or a safe default) and sets the title based on the exception type (SystemException uses its own title;
     * ConstraintViolationException uses the BAD_REQUEST reason phrase; all others use a generic default title).
     *
     * @param exception  the exception being represented
     * @param statusCode the HTTP status code to attach to the ProblemDetail
     * @return a populated ProblemDetail instance
     */
    private ProblemDetail createProblemDetail(final Exception exception,
        final HttpStatusCode statusCode) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(statusCode);
        problemDetail.setDetail(getMessageFromException(exception));
        if (exception instanceof SystemException) {
            problemDetail.setTitle(((SystemException) exception).getTitle());
        } else if (exception instanceof ConstraintViolationException) {
            problemDetail.setTitle(HttpStatus.BAD_REQUEST.getReasonPhrase());
        } else {
            problemDetail.setTitle(DEFAULT_TITLE);
        }
        return problemDetail;
    }

    /**
     * Obtain a safe message string from an exception. If the exception message is blank a default user-friendly message
     * is returned instead of an empty or null string.
     *
     * @param exception the exception whose message is required
     * @return a non-blank message suitable for inclusion in ProblemDetail.detail
     */
    private String getMessageFromException(final Exception exception) {
        if (StringUtils.isNotBlank(exception.getMessage())) {
            return exception.getMessage();
        }
        return DEFAULT_MESSAGE;
    }

    /**
     * Map the numeric status embedded inside a {@link SystemException} to an {@link HttpStatusCode}. If mapping fails
     * the method logs the issue using the configured audit logger and returns
     * {@link HttpStatus#INTERNAL_SERVER_ERROR}.
     *
     * @param exception the SystemException carrying a numeric status code
     * @return the mapped HttpStatusCode or INTERNAL_SERVER_ERROR when mapping fails
     */
    private HttpStatusCode getHttpStatusCodeFromSystemException(final SystemException exception) {
        try {
            return HttpStatusCode.valueOf(exception.getStatusCode());
        } catch (final IllegalArgumentException iae) {
            if (LOG.isErrorEnabled()) {
                logger.error(LOG, ERROR_MESSAGE + exception.getStatusCode());
            }
            return INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * Convert certain known exception types to an HTTP status code. Supported conversions:
     * <ul>
     *   <li>{@link HttpClientErrorException} -> its embedded status code
     *   <li>{@link HttpRequestMethodNotSupportedException} -> 405 Method Not Allowed
     *   <li>otherwise -> 500 Internal Server Error
     * </ul>
     *
     * @param exception the exception to map
     * @return an appropriate HttpStatusCode
     */
    private HttpStatusCode getHttpStatusCodeFromException(final Exception exception) {
        if (exception instanceof HttpClientErrorException) {
            return ((HttpClientErrorException) exception).getStatusCode();
        } else if (exception instanceof HttpRequestMethodNotSupportedException) {
            return METHOD_NOT_ALLOWED;
        }
        return INTERNAL_SERVER_ERROR;
    }
}

