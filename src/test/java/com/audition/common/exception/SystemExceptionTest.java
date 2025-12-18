package com.audition.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SystemExceptionTest {

    public static final String MSG = "msg";
    public static final String TITLE = "title";
    public static final String DETAIL = "detail";

    @Test
    void constructorsSetFieldsCorrectly() {
        final var e2 = new SystemException(MSG);
        assertThat(e2.getMessage()).isEqualTo(MSG);
        assertThat(e2.getTitle()).isEqualTo(SystemException.DEFAULT_TITLE);

        final var e3 = new SystemException(MSG, 400);
        assertThat(e3.getMessage()).isEqualTo(MSG);
        assertThat(e3.getStatusCode()).isEqualTo(400);

        final var e4 = new SystemException(DETAIL, TITLE, 401);
        assertThat(e4.getDetail()).isEqualTo(DETAIL);
        assertThat(e4.getTitle()).isEqualTo(TITLE);
        assertThat(e4.getStatusCode()).isEqualTo(401);
    }

    @Test
    void testExceptionLoggers() {
        final var e5 = new SystemException(DETAIL, TITLE, 402, new RuntimeException("cause"));
        assertThat(e5.getDetail()).isEqualTo(DETAIL);
        assertThat(e5.getStatusCode()).isEqualTo(402);

        final var e6 = new SystemException(DETAIL, new RuntimeException("cause"));
        assertThat(e6.getMessage()).isEqualTo(DETAIL);

        final var e7 = new SystemException(DETAIL, TITLE, new RuntimeException("cause"));
        assertThat(e7.getTitle()).isEqualTo(TITLE);
        assertThat(e7.getStatusCode()).isEqualTo(500);
    }
}

