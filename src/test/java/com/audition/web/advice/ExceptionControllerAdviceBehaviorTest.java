package com.audition.web.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import java.lang.reflect.Field;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ProblemDetail;

@ExtendWith(MockitoExtension.class)
@Getter
@Setter
class ExceptionControllerAdviceBehaviorTest {

    private ExceptionControllerAdvice advice;

    @Mock
    private AuditionLogger mockLogger;

    @BeforeEach
    void setUp() throws Exception {
        advice = new ExceptionControllerAdvice(mockLogger);
        final Field loggerField = ExceptionControllerAdvice.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(advice, mockLogger);
    }

    @Test
    void handleSystemExceptionWithInvalidStatusLogsAndReturns500() {
        final SystemException ex = new SystemException("d", "t", 9999);

        final ProblemDetail pd = advice.handleSystemException(ex);

        assertThat(pd.getStatus()).isEqualTo(500);
    }
}
