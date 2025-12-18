package com.audition.configuration;

import com.audition.common.interceptor.AuditionClientHttpRequestInterceptor;
import com.audition.common.logging.AuditionLogger;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.text.SimpleDateFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Central web / REST configuration for the application.
 *
 * <p>This configuration exposes commonly used HTTP-related beans:
 * <ul>
 *   <li>An {@link ObjectMapper} configured for the application's JSON needs (date format,
 *       lenient deserialization, camelCase property names, no null/empty values, etc.).
 *   <li>A {@link RestTemplate} wired with a buffering request factory, a JSON message
 *       converter that uses the configured {@link ObjectMapper} and a request interceptor
 *       that logs requests/responses.
 *   <li>A convenience {@link MappingJackson2HttpMessageConverter} that uses the configured
 *       mapper so Spring MVC and the RestTemplate share the same serialization configuration.
 * </ul>
 *
 * <p>All beans are intentionally simple and focused on testability (objects are created
 * programmatically rather than discovered) so unit tests can instantiate and assert
 * configuration easily.
 */
@Configuration
@RequiredArgsConstructor
public class WebServiceConfiguration implements WebMvcConfigurer {

    private static final String YEAR_MONTH_DAY_PATTERN = "yyyy-MM-dd";
    private final AuditionLogger auditLogger;

    /**
     * Create and configure the application's Jackson {@link ObjectMapper}.
     *
     * <p>Configuration applied:
     * <ul>
     *   <li>Use date format yyyy-MM-dd
     *   <li>Do not fail on unknown properties during deserialization
     *   <li>Use lower camel case property naming
     *   <li>Exclude null and empty values from serialization
     *   <li>Write dates as ISO dates (not timestamps)
     *   <li>Enable pretty-print (INDENT_OUTPUT) to make debug output human-readable
     * </ul>
     *
     * @return a configured {@link ObjectMapper}
     */
    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        //  1. allows for date format as yyyy-MM-dd
        objectMapper.setDateFormat(new SimpleDateFormat(YEAR_MONTH_DAY_PATTERN, java.util.Locale.getDefault()));
        //  2. Does not fail on unknown properties
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //  3. maps to camelCase
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        //  4. Does not include null values or empty values
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        //  5. does not write datas as timestamps. - hope this is date not datas
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }

    /**
     * Create a {@link RestTemplate} pre-configured for the application.
     *
     * <p>The rest template uses a buffering request factory so the response body can be
     * read by interceptors for logging without consuming the stream permanently. It also registers the application's
     * JSON message converter and a request interceptor.
     *
     * @param builder a {@link RestTemplateBuilder} provided by Spring
     * @return a configured {@link RestTemplate}
     */
    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder builder) {
        return builder
            .requestFactory(() -> new BufferingClientHttpRequestFactory(createClientFactory()))
            .messageConverters(jackson2HttpMessageConverter())
            .interceptors(auditionInterceptor())
            .build();
    }

    /**
     * Create a low-level HTTP client factory used by the buffering request factory. The factory disables output
     * streaming to allow buffering of requests where needed.
     *
     * @return a configured {@link SimpleClientHttpRequestFactory}
     */
    private SimpleClientHttpRequestFactory createClientFactory() {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setOutputStreaming(false);
        return requestFactory;
    }

    /**
     * Create a Jackson message converter wired with the application's {@link ObjectMapper}. This ensures consistent
     * serialization/deserialization behavior between MVC and RestTemplate.
     *
     * @return a configured {@link MappingJackson2HttpMessageConverter}
     */
    @Bean
    public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    /**
     * Create the {@link AuditionClientHttpRequestInterceptor} used to log outgoing RestTemplate calls.
     *
     * @return a new interceptor instance
     */
    @Bean
    public AuditionClientHttpRequestInterceptor auditionInterceptor() {
        return new AuditionClientHttpRequestInterceptor(auditLogger);
    }

}
