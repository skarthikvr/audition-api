package com.audition.configuration;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that injects tracing identifiers into the HTTP response headers.
 *
 * <p>This filter executes once per request (via {@link OncePerRequestFilter}) and will
 * add two headers to the response when they are not already present:
 * <ul>
 *   <li><b>trace-id</b> — the current trace id from the configured {@link Tracer}.
 *   <li><b>span-id</b> — the current span id from the configured {@link Tracer}.
 * </ul>
 *
 * <p>Behavior notes:
 * <ul>
 *   <li>If the response already contains a <code>trace-id</code> header this filter will not
 *       overwrite it.
 *   <li>The filter safely handles the absence of an active tracing context — it only injects
 *       headers when {@code tracer.currentTraceContext().context()} is present.
 *   <li>This class is marked as a Spring {@link Component} so it is picked up by component
 *       scanning. It is safe to use as a singleton bean because it holds no mutable state.
 * </ul>
 *
 * <p>Usage:
 * <ul>
 *   <li>Registering this component in a Spring Boot application will ensure trace and span
 *       identifiers are exposed on every HTTP response (useful for correlating logs with
 *       downstream requests and diagnostic traces).
 *   <li>In environments where tracing is not configured, the filter will simply do nothing.
 * </ul>
 */
@Component
@Getter
@Setter
public class ResponseHeaderInjector extends OncePerRequestFilter {

    private final Tracer tracer;

    /**
     * Create a new filter which uses the supplied {@link Tracer} to obtain the current trace/span ids when available.
     *
     * @param tracer the Micrometer tracer used to access the current trace context (may be null in tests or non-tracing
     *               environments)
     */
    public ResponseHeaderInjector(final Tracer tracer) {
        super();
        this.tracer = tracer;
    }

    /**
     * Called once per request. If the response does not already contain a <code>trace-id</code> header this method
     * attempts to obtain the current trace context from the configured {@link Tracer} and injects <code>trace-id</code>
     * and <code>span-id</code> headers when a context is present.
     *
     * <p>The filter always delegates to the {@link FilterChain} after performing header injection
     * so normal request processing continues.
     *
     * @param request     the incoming servlet request
     * @param response    the servlet response where trace headers will be set
     * @param filterChain the chain to continue request processing
     * @throws ServletException if the downstream filter throws a ServletException
     * @throws IOException      if an I/O error occurs during filter processing
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain filterChain) throws ServletException, IOException {

        if (!response.getHeaderNames().contains("trace-id")) {
            Optional.ofNullable(tracer)
                .map(Tracer::currentTraceContext)
                .map(ct -> ct.context())
                .ifPresent(context -> {
                    response.setHeader("trace-id", context.traceId());
                    response.setHeader("span-id", context.spanId());
                });
        }

        filterChain.doFilter(request, response);
    }
}
