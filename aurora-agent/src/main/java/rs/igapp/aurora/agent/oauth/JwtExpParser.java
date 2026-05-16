package rs.igapp.aurora.agent.oauth;

import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class JwtExpParser {

    private JwtExpParser() {}

    static Optional<Instant> parseExp(String jwt, ObjectMapper mapper) {
        if (jwt == null || jwt.isBlank()) {
            return Optional.empty();
        }
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) {
            return Optional.empty();
        }
        try {
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode n = mapper.readTree(payload);
            if (n.has("exp") && n.get("exp").canConvertToLong()) {
                return Optional.of(Instant.ofEpochSecond(n.get("exp").asLong()));
            }
        } catch (Exception ex) {
            return Optional.empty();
        }
        return Optional.empty();
    }
}
