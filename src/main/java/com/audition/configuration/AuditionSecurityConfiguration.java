package com.audition.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Central Spring Security configuration for the Audition application.
 *
 * <p>This configuration exposes a {@link SecurityFilterChain} bean that controls access
 * to application endpoints. The intent is to permit anonymous access to basic health and info endpoints while
 * protecting other actuator endpoints and leaving other application endpoints open by default.
 *
 * <ul>
 *   <li>Permit access to <code>/actuator/health</code> and <code>/actuator/info</code> to support
 *       health checks from load balancers and platform probes.
 *   <li>Require authentication for the remaining actuator endpoints under <code>/actuator/**</code>.
 *   <li>Allow access to any other request (application endpoints) by default.
 *   <li>Use HTTP Basic authentication for protected endpoints (simple, suitable for internal
 *       or demo scenarios; replace or augment with a stronger mechanism in production).
 * </ul>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Getter
@Setter
public class AuditionSecurityConfiguration {

    /**
     * Create the application SecurityFilterChain.
     *
     * <p>The chain is configured to:
     * <ol>
     *   <li>Allow unauthenticated access to <code>/actuator/health</code> and <code>/actuator/info</code>.</li>
     *   <li>Require authentication for any other <code>/actuator/**</code> endpoints.</li>
     *   <li>Permit all other requests by default.</li>
     * </ol>
     *
     * @param http the {@link HttpSecurity} builder provided by Spring Security
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception when configuration cannot be built
     */
    @SneakyThrows
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) {
        http
            .authorizeHttpRequests((auth) -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**")
                .authenticated()
                .anyRequest().permitAll()
            ).httpBasic();
        return http.build();
    }

}
