package com.audition.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
@Getter
@Setter
class ResponseHeaderInjectorTest {

    @Mock
    private Tracer mockTracer;

    @Mock
    private CurrentTraceContext mockCurrent;

    @Mock
    private TraceContext mockContext;

    @Mock
    private FilterChain mockChain;

    @Mock
    private HttpServletRequest mockRequest;

    private ResponseHeaderInjector filter;

    @BeforeEach
    void setUp() {
        filter = new ResponseHeaderInjector(mockTracer);
    }

    @Test
    @SneakyThrows
    void doFilterInternalSetsTraceAndSpanHeadersWhenAbsent() {
        when(mockTracer.currentTraceContext()).thenReturn(mockCurrent);
        when(mockCurrent.context()).thenReturn(mockContext);
        when(mockContext.traceId()).thenReturn("tid");
        when(mockContext.spanId()).thenReturn("sid");

        final MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(mockRequest, response, mockChain);

        assertThat(response.getHeader("trace-id")).isEqualTo("tid");
        assertThat(response.getHeader("span-id")).isEqualTo("sid");
    }

    @Test
    @SneakyThrows
    void doFilterInternalNoContextDoesNotSetHeaders() {
        when(mockTracer.currentTraceContext()).thenReturn(mockCurrent);
        when(mockCurrent.context()).thenReturn(null);

        final MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(mockRequest, response, mockChain);

        assertThat(response.getHeaderNames()).doesNotContain("trace-id");
    }
}
