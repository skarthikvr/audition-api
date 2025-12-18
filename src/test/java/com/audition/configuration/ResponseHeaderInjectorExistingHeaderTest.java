package com.audition.configuration;

import static org.assertj.core.api.Assertions.assertThat;

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
class ResponseHeaderInjectorExistingHeaderTest {

    @Mock
    private FilterChain mockChain;

    @Mock
    private HttpServletRequest mockRequest;

    private ResponseHeaderInjector filter;

    @BeforeEach
    void setUp() {
        filter = new ResponseHeaderInjector(null);
    }

    @Test
    @SneakyThrows
    void doFilterInternalDoesNotOverwriteExistingTraceId() {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setHeader("trace-id", "existing");

        filter.doFilterInternal(mockRequest, response, mockChain);

        assertThat(response.getHeader("trace-id")).isEqualTo("existing");
    }
}
