package com.audition.web.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import jakarta.validation.ConstraintViolationException;
import java.lang.reflect.Field;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
@Getter
@Setter
class ExceptionControllerAdviceTest {

    private ExceptionControllerAdvice advice;

    @Mock
    private AuditionLogger mockLogger;

    @BeforeEach
    void setUp() throws Exception {
        advice = new ExceptionControllerAdvice(mockLogger);
        // inject mock audit logger into private field
        final Field loggerField = ExceptionControllerAdvice.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(advice, mockLogger);
    }

    @Test
    void handleSystemExceptionWithValidStatusReturnsProblemDetailWithTitle() {
        final SystemException ex = new SystemException("detail", "MyTitle", 400);

        final ProblemDetail pd = advice.handleSystemException(ex);

        assertThat(pd.getTitle()).isEqualTo("MyTitle");
        assertThat(pd.getDetail()).isEqualTo("detail");
        assertThat(pd.getStatus()).isEqualTo(400);
    }

    @Test
    void handleSystemExceptionWithInvalidStatusReturnsInternalServerErrorAndLogs() {
        final SystemException ex = new SystemException("detail", "Title", 9999);

        final ProblemDetail pd = advice.handleSystemException(ex);

        assertThat(pd.getStatus()).isEqualTo(500);
    }

    @Test
    void handleMainExceptionWithHttpClientErrorExceptionReturnsStatusFromException() {
        final HttpClientErrorException clientEx = new HttpClientErrorException(
            org.springframework.http.HttpStatus.NOT_FOUND, "Not Found");

        final ProblemDetail pd = advice.handleMainException(clientEx);

        assertThat(pd.getStatus()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND.value());
    }

    @Test
    void handleMainExceptionWithMethodNotAllowedReturnsMethodNotAllowed() {
        final HttpRequestMethodNotSupportedException e = new HttpRequestMethodNotSupportedException("GET");

        final ProblemDetail pd = advice.handleMainException(e);

        assertThat(pd.getStatus()).isEqualTo(org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED.value());
    }

    @Test
    void handleMainExceptionWithConstraintViolationReturnsBadRequestTitle() {
        final ConstraintViolationException e = new ConstraintViolationException(null);

        final ProblemDetail pd = advice.handleMainException(e);

        assertThat(pd.getTitle()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(pd.getStatus()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void testMessageFromExceptionReturnsDefaultWhenBlank() {
        final Exception e = new Exception("");
        final ProblemDetail pd = advice.handleMainException(e);
        assertThat(pd.getDetail()).isEqualTo("API Error occurred. Please contact support or administrator.");
    }
}
