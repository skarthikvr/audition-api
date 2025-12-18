package com.audition.common.logging;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class AuditionLogger {

    public void info(final Logger logger, final String message) {
        if (logger.isInfoEnabled()) {
            logger.info(message);
        }
    }

    public void info(final Logger logger, final String message, final Object object) {
        if (logger.isInfoEnabled()) {
            logger.info(message, object);
        }
    }

    public void debug(final Logger logger, final String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    public void warn(final Logger logger, final String message) {
        if (logger.isWarnEnabled()) {
            logger.warn(message);
        }
    }

    public void error(final Logger logger, final String message) {
        if (logger.isErrorEnabled()) {
            logger.error(message);
        }
    }

    public void logErrorWithException(final Logger logger, final String message, final Exception e) {
        if (logger.isErrorEnabled()) {
            logger.error(message, e);
        }
    }

    public void logStandardProblemDetail(final Logger logger, final ProblemDetail problemDetail, final Exception e) {
        if (logger.isErrorEnabled()) {
            final var message = createStandardProblemDetailMessage(problemDetail);
            logger.error(message, e);
        }
    }

    public void logHttpStatusCodeError(final Logger logger, final String message, final Integer errorCode) {
        if (logger.isErrorEnabled()) {
            logger.error(createBasicErrorResponseMessage(errorCode, message) + "\n");
        }
    }

    private String createStandardProblemDetailMessage(final ProblemDetail standardProblemDetail) {
        if (standardProblemDetail == null) {
            return StringUtils.EMPTY;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("ProblemDetail:");
        if (standardProblemDetail.getType() != null) {
            sb.append(" type=").append(standardProblemDetail.getType());
        }
        if (standardProblemDetail.getTitle() != null) {
            sb.append(" title=").append(standardProblemDetail.getTitle());
        }
        if (standardProblemDetail.getStatus() > 0) {
            sb.append(" status=").append(standardProblemDetail.getStatus());
        }
        if (standardProblemDetail.getDetail() != null) {
            sb.append(" detail=").append(standardProblemDetail.getDetail());
        }
        return sb.toString();
    }

    private String createBasicErrorResponseMessage(final Integer errorCode, final String message) {
        final StringBuilder sb = new StringBuilder();
        if (errorCode != null) {
            sb.append("errorCode=").append(errorCode);
        }
        if (StringUtils.isNotBlank(message)) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append("message=").append(message);
        }
        return sb.toString();
    }
}
