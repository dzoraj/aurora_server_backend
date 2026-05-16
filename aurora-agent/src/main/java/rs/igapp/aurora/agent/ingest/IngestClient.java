package rs.igapp.aurora.agent.ingest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import rs.igapp.aurora.agent.config.AgentProperties;
import rs.igapp.aurora.agent.oauth.AccessTokenProvider;
import rs.igapp.aurora.api.dto.request.LogEventRequest;

@Component
public class IngestClient {

    public static final String INGEST_KEY_HEADER = "X-Aurora-Ingest-Key";

    private static final Logger log = LoggerFactory.getLogger(IngestClient.class);

    private final AgentProperties properties;
    private final AccessTokenProvider tokens;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public IngestClient(
            AgentProperties properties,
            AccessTokenProvider tokens,
            ObjectMapper objectMapper,
            RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.tokens = tokens;
        this.objectMapper = objectMapper;
        String base = trimTrailingSlash(properties.getApiBaseUrl());
        this.restClient =
                restClientBuilder
                        .baseUrl(base)
                        .requestInterceptor(
                                (request, body, execution) -> {
                                    if (StringUtils.hasText(properties.getIngestApiKey())) {
                                        request.getHeaders().set(
                                                INGEST_KEY_HEADER,
                                                properties.getIngestApiKey().strip());
                                    } else {
                                        request.getHeaders().setBearerAuth(tokens.getAccessToken());
                                    }
                                    return execution.execute(request, body);
                                })
                        .build();
    }


    public boolean shipLine(String line) {
        String message =
                (properties.getMessagePrefix() == null || properties.getMessagePrefix().isBlank())
                        ? line
                        : properties.getMessagePrefix() + line;
        String raw;
        try {
            raw =
                    objectMapper.writeValueAsString(
                            java.util.Map.of(
                                    "agent", "aurora-agent",
                                    "receivedAt", OffsetDateTime.now(ZoneOffset.UTC).toString()));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            raw = null;
        }
        LogEventRequest body =
                LogEventRequest.builder()
                        .sourceId(properties.getSourceId())
                        .message(message)
                        .severityId(properties.getSeverityId())
                        .rawData(raw)
                        .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                        .build();
        try {
            restClient
                    .post()
                    .uri("/api/log-events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Shipped log line ({} chars)", message.length());
            return true;
        } catch (RestClientResponseException ex) {
            log.error(
                    "Ingest failed HTTP {}: {}",
                    ex.getStatusCode().value(),
                    truncate(ex.getResponseBodyAsString(), 500));
            return false;
        } catch (RuntimeException ex) {
            log.error("Ingest failed: {}", ex.getMessage());
            return false;
        }
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:8080";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
