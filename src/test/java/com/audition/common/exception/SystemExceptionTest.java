package com.audition.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SystemExceptionTest {

    @Test
    void constructorsSetFieldsCorrectly() {
        final var e2 = new SystemException("msg");
        assertThat(e2.getMessage()).isEqualTo("msg");
        assertThat(e2.getTitle()).isEqualTo(SystemException.DEFAULT_TITLE);

        final var e3 = new SystemException("msg", 400);
        assertThat(e3.getMessage()).isEqualTo("msg");
        assertThat(e3.getStatusCode()).isEqualTo(400);

        final var e4 = new SystemException("detail", "title", 401);
        assertThat(e4.getDetail()).isEqualTo("detail");
        assertThat(e4.getTitle()).isEqualTo("title");
        assertThat(e4.getStatusCode()).isEqualTo(401);
    }

    @Test
    void testExceptionLoggers() {
        final var e5 = new SystemException("detail", "title", 402, new RuntimeException("cause"));
        assertThat(e5.getDetail()).isEqualTo("detail");
        assertThat(e5.getStatusCode()).isEqualTo(402);

        final var e6 = new SystemException("detail", new RuntimeException("cause"));
        assertThat(e6.getMessage()).isEqualTo("detail");

        final var e7 = new SystemException("detail", "title", new RuntimeException("cause"));
        assertThat(e7.getTitle()).isEqualTo("title");
        assertThat(e7.getStatusCode()).isEqualTo(500);
    }
}

