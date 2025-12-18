package com.audition.common.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.ProblemDetail;

@ExtendWith(MockitoExtension.class)
@Getter
@Setter
class AuditionLoggerTest {

    private AuditionLogger auditionLogger;

    @Mock
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        auditionLogger = new AuditionLogger();
    }

    @Test
    void infoDoesNotThrowWhenInfoDisabled() {
        when(mockLogger.isInfoEnabled()).thenReturn(false);
        auditionLogger.info(mockLogger, "hi");
    }

    @Test
    void infoWithObjectCallsWhenEnabled() {
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        auditionLogger.info(mockLogger, "msg {}", "x");
    }

    @Test
    @SneakyThrows
    void createStandardProblemDetailMessageBuildsExpectedString() {
        final ProblemDetail pd = ProblemDetail.forStatus(400);
        pd.setDetail("details");
        pd.setTitle("title");
        final Method m = AuditionLogger.class.getDeclaredMethod("createStandardProblemDetailMessage",
            ProblemDetail.class);
        m.setAccessible(true);
        final String result = (String) m.invoke(auditionLogger, pd);
        assertThat(result).contains("ProblemDetail").contains("title").contains("detail");
    }

    @Test
    void createBasicErrorResponseMessageBuildsExpectedString() throws Exception {
        final Method m = AuditionLogger.class.getDeclaredMethod("createBasicErrorResponseMessage", Integer.class,
            String.class);
        m.setAccessible(true);
        final String result = (String) m.invoke(auditionLogger, 404, "oops");
        assertThat(result).contains("errorCode=404").contains("message=oops");
    }
}
