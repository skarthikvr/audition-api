package com.audition.common.logging;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lombok.Getter;
import lombok.Setter;
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
class AuditionLoggerBehaviorTest {

    private AuditionLogger auditionLogger;

    @Mock
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        auditionLogger = new AuditionLogger();
    }

    @Test
    void logStandardProblemDetailCallsLoggerErrorWhenEnabled() {
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        final ProblemDetail pd = ProblemDetail.forStatus(400);
        pd.setTitle("t");
        pd.setDetail("d");

        auditionLogger.logStandardProblemDetail(mockLogger, pd, new RuntimeException("boom"));

        verify(mockLogger).error(org.mockito.ArgumentMatchers.contains("ProblemDetail"),
            org.mockito.ArgumentMatchers.any(Exception.class));
    }

    @Test
    void logHttpStatusCodeErrorCallsLoggerErrorWhenEnabled() {
        when(mockLogger.isErrorEnabled()).thenReturn(true);

        auditionLogger.logHttpStatusCodeError(mockLogger, "message", 500);

        verify(mockLogger).error(org.mockito.ArgumentMatchers.contains("errorCode=500"));
    }
}
