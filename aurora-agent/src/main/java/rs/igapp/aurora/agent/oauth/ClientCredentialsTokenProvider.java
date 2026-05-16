package rs.igapp.aurora.agent.oauth;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public final class ClientCredentialsTokenProvider implements AccessTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(ClientCredentialsTokenProvider.class);

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper;
    private final URI tokenUri;
    private final String clientId;
    private final String clientSecret;

    private String cached;
    private Instant cachedUntil = Instant.EPOCH;

    public ClientCredentialsTokenProvider(
            ObjectMapper mapper, String tokenUri, String clientId, String clientSecret) {
        this.mapper = mapper;
        this.tokenUri = URI.create(tokenUri);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public synchronized String getAccessToken() {
        Instant now = Instant.now();
        if (cached != null && now.isBefore(cachedUntil)) {
            return cached;
        }
        String form =
                join(
                        "grant_type",
                        "client_credentials",
                        "client_id",
                        clientId,
                        "client_secret",
                        clientSecret);
        HttpRequest request =
                HttpRequest.newBuilder(tokenUri)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(form))
                        .build();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException(
                        "Token endpoint HTTP " + response.statusCode() + ": " + response.body());
            }
            JsonNode root = mapper.readTree(response.body());
            String access = root.path("access_token").asText(null);
            if (access == null || access.isBlank()) {
                throw new IllegalStateException("Token response missing access_token");
            }
            long expiresIn = root.path("expires_in").asLong(300);
            Instant until =
                    JwtExpParser.parseExp(access, mapper)
                            .map(exp -> exp.minusSeconds(30))
                            .orElseGet(() -> now.plusSeconds(Math.max(60, expiresIn - 30)));
            this.cached = access;
            this.cachedUntil = until.isAfter(now) ? until : now.plusSeconds(60);
            log.debug("Obtained access token (client_credentials), refresh before {}", cachedUntil);
            return cached;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to obtain access token", e);
        }
    }

    private static String join(String k1, String v1, String k2, String v2, String k3, String v3) {
        return enc(k1, v1) + "&" + enc(k2, v2) + "&" + enc(k3, v3);
    }

    private static String enc(String k, String v) {
        return URLEncoder.encode(k, StandardCharsets.UTF_8)
                + "="
                + URLEncoder.encode(v, StandardCharsets.UTF_8);
    }
}
