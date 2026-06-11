package rs.igapp.aurora.server;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import rs.igapp.aurora.server.security.AgentIngestApiKeyAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Value("${aurora.security.permit-all:true}")
    private boolean permitAll;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Value("${aurora.security.cors-allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    /**
     * Comma-separated shared secret(s) for {@code POST /api/log-events} via header
     * {@link AgentIngestApiKeyAuthenticationFilter#HEADER_NAME}. Empty = disabled (JWT only).
     */
    @Value("${aurora.security.agent-ingest-api-keys:}")
    private String agentIngestApiKeys;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.cors(cors -> cors.configurationSource(buildCorsConfigurationSource()));

        if (permitAll) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        if (issuerUri == null || issuerUri.isBlank()) {
            throw new IllegalStateException(
                    "When aurora.security.permit-all=false you must set "
                            + "spring.security.oauth2.resourceserver.jwt.issuer-uri "
                            + "(e.g. http://localhost:8180/realms/aurora)");
        }

        http.authorizeHttpRequests(
                auth ->
                        auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                                .permitAll()
                                .anyRequest()
                                .authenticated());
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        List<String> ingestKeys = parseIngestApiKeys(agentIngestApiKeys);
        if (!ingestKeys.isEmpty()) {
            log.info(
                    "Agent ingest key auth enabled for POST /api/log-events ({} configured key(s), header {}).",
                    ingestKeys.size(),
                    AgentIngestApiKeyAuthenticationFilter.HEADER_NAME);
            http.addFilterBefore(
                    new AgentIngestApiKeyAuthenticationFilter(ingestKeys),
                    BearerTokenAuthenticationFilter.class);
        } else if (agentIngestApiKeys != null && !agentIngestApiKeys.isBlank()) {
            log.warn(
                    "aurora.security.agent-ingest-api-keys is non-empty but no keys parsed after split/trim; "
                            + "POST /api/log-events accepts JWT only. Remove surrounding quotes from the value.");
        }
        return http.build();
    }

    private static List<String> parseIngestApiKeys(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(SecurityConfiguration::stripOptionalQuotes)
                .filter(StringUtils::hasText)
                .toList();
    }

    private static String stripOptionalQuotes(String s) {
        if (s.length() >= 2) {
            char a = s.charAt(0);
            char z = s.charAt(s.length() - 1);
            if ((a == '\"' && z == '\"') || (a == '\'' && z == '\'')) {
                return s.substring(1, s.length() - 1).trim();
            }
        }
        return s;
    }

    private CorsConfigurationSource buildCorsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins =
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
