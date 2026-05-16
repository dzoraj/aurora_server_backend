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


public final class PasswordGrantTokenProvider implements AccessTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(PasswordGrantTokenProvider.class);

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper;
    private final URI tokenUri;
    private final String clientId;
    private final String username;
    private final String password;

    private String cached;
    private Instant cachedUntil = Instant.EPOCH;

    public PasswordGrantTokenProvider(
            ObjectMapper mapper,
            String tokenUri,
            String clientId,
            String username,
            String password) {
        this.mapper = mapper;
        this.tokenUri = URI.create(tokenUri);
        this.clientId = clientId;
        this.username = username;
        this.password = password;
    }

    @Override
    public synchronized String getAccessToken() {
        Instant now = Instant.now();
        if (cached != null && now.isBefore(cachedUntil)) {
            return cached;
        }
        String form =
                enc("grant_type", "password")
                        + "&"
                        + enc("client_id", clientId)
                        + "&"
                        + enc("username", username)
                        + "&"
                        + enc("password", password);
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
            log.debug("Obtained access token (password grant), refresh before {}", cachedUntil);
            return cached;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to obtain access token", e);
        }
    }

    private static String enc(String k, String v) {
        return URLEncoder.encode(k, StandardCharsets.UTF_8)
                + "="
                + URLEncoder.encode(v, StandardCharsets.UTF_8);
    }
}
