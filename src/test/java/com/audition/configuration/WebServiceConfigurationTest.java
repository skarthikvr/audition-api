package com.audition.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import com.audition.common.logging.AuditionLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class WebServiceConfigurationTest {

    @Mock
    private AuditionLogger mockLogger;

    @Test
    void objectMapper_configured_asExpected() {
        final WebServiceConfiguration cfg = new WebServiceConfiguration(mockLogger);
        final ObjectMapper mapper = cfg.objectMapper();
        assertThat(mapper).isNotNull();
        assertThat(mapper.getSerializationConfig()
            .isEnabled(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT)).isTrue();
        assertThat(mapper.getSerializationConfig()
            .isEnabled(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();
    }

    @Test
    void jacksonConverter_usesObjectMapper() {
        final WebServiceConfiguration cfg = new WebServiceConfiguration(mockLogger);
        final var converter = cfg.jackson2HttpMessageConverter();
        final ObjectMapper mapperFromConverter = converter.getObjectMapper();
        assertThat(mapperFromConverter).isNotNull();
        // ensure the converter's mapper is configured similarly
        assertThat(mapperFromConverter.getSerializationConfig()
            .isEnabled(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT)).isTrue();
        assertThat(mapperFromConverter.getSerializationConfig()
            .isEnabled(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();
    }
}
