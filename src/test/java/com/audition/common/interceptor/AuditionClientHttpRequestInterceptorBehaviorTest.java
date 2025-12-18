package com.audition.common.interceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.audition.common.logging.AuditionLogger;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

@ExtendWith(MockitoExtension.class)
@Getter
@Setter
class AuditionClientHttpRequestInterceptorBehaviorTest {

    private AuditionClientHttpRequestInterceptor interceptor;

    @Mock
    private AuditionLogger mockAuditLogger;

    @Mock
    private ClientHttpRequestExecution mockExecution;

    @Mock
    private ClientHttpResponse mockResponse;

    @Mock
    private HttpRequest mockRequest;

    @BeforeEach
    void setUp() {
        interceptor = new AuditionClientHttpRequestInterceptor(mockAuditLogger);
    }

    @Test
    @SuppressWarnings("resource")
    void intercept_usesAuditLogger_toLog() throws Exception {
        when(mockResponse.getBody()).thenReturn(new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));
        when(mockResponse.getHeaders()).thenReturn(new HttpHeaders());
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockExecution.execute(any(HttpRequest.class), any(byte[].class))).thenReturn(mockResponse);

        interceptor.intercept(mockRequest, new byte[0], mockExecution);

        // The interceptor logs multiple messages; ensure at least one info call with three args occurred
        verify(mockAuditLogger, atLeastOnce()).info(any(), any(), any());
    }
}
