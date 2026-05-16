package rs.igapp.aurora.agent.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import rs.igapp.aurora.agent.oauth.AccessTokenProvider;
import rs.igapp.aurora.agent.oauth.ClientCredentialsTokenProvider;
import rs.igapp.aurora.agent.oauth.PasswordGrantTokenProvider;
import rs.igapp.aurora.agent.oauth.StaticAccessTokenProvider;

@Configuration
public class AgentConfiguration {

    @Bean
    @ConditionalOnMissingBean(RestClient.Builder.class)
    RestClient.Builder restClientBuilderFallback() {
        return RestClient.builder();
    }

    @Bean
    AccessTokenProvider accessTokenProvider(AgentProperties properties, ObjectMapper objectMapper) {
        if (StringUtils.hasText(properties.getIngestApiKey())) {
            return () -> "";
        }
        AgentProperties.OAuth o = properties.getOauth();
        if (StringUtils.hasText(o.getAccessToken())) {
            return new StaticAccessTokenProvider(o.getAccessToken());
        }
        if (!StringUtils.hasText(o.getTokenUri()) || !StringUtils.hasText(o.getClientId())) {
            throw new IllegalStateException(
                    "Missing auth: set aurora.agent.ingest-api-key (env AURORA_AGENT_INGEST_API_KEY) to match "
                            + "server aurora.security.agent-ingest-api-keys, or set oauth.access-token (env "
                            + "AURORA_AGENT_OAUTH_ACCESS_TOKEN), or configure token-uri + client-id with "
                            + "client-secret (client_credentials) or username + password (password grant). "
                            + "See aurora-agent/README.md.");
        }
        if (StringUtils.hasText(o.getClientSecret())) {
            return new ClientCredentialsTokenProvider(
                    objectMapper, o.getTokenUri(), o.getClientId(), o.getClientSecret());
        }
        if (StringUtils.hasText(o.getUsername()) && StringUtils.hasText(o.getPassword())) {
            return new PasswordGrantTokenProvider(
                    objectMapper, o.getTokenUri(), o.getClientId(), o.getUsername(), o.getPassword());
        }
        throw new IllegalStateException(
                "Incomplete OAuth: with token-uri + client-id you must set client-secret OR both username and "
                        + "password, or use aurora.agent.ingest-api-key / oauth.access-token instead.");
    }
}
