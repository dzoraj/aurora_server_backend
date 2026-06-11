package rs.igapp.aurora.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lets {@code POST /api/log-events} authenticate via {@value #HEADER_NAME} instead of JWT,
 * when one or more API keys are configured.
 */
public class AgentIngestApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Aurora-Ingest-Key";

    public static final String ROLE_AGENT_INGEST = "ROLE_AGENT_INGEST";

    private static final Logger log = LoggerFactory.getLogger(AgentIngestApiKeyAuthenticationFilter.class);

    private final List<byte[]> configuredKeysUtf8;

    public AgentIngestApiKeyAuthenticationFilter(List<String> plaintextKeys) {
        if (plaintextKeys.isEmpty()) {
            throw new IllegalArgumentException("expected at least one API key");
        }
        this.configuredKeysUtf8 =
                plaintextKeys.stream().map(k -> k.getBytes(StandardCharsets.UTF_8)).toList();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!matchesIngestEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        byte[] provided = normalizeHeader(request.getHeader(HEADER_NAME));
        boolean ok =
                configuredKeysUtf8.stream()
                        .anyMatch(secret -> secretsEqual(secret, provided));
        if (!ok) {
            if (provided.length > 0) {
                log.warn("Rejected ingest: invalid {}", HEADER_NAME);
            }
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "ingest-agent",
                        null,
                        List.of(new SimpleGrantedAuthority(ROLE_AGENT_INGEST)));
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    private static boolean matchesIngestEndpoint(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        if (!"/api/log-events".equals(uri)) {
            String ctx = request.getContextPath();
            if (ctx != null && !ctx.isEmpty()) {
                return (ctx + "/api/log-events").equals(uri);
            }
            return false;
        }
        String ct = request.getContentType();
        if (ct != null && !ct.strip().startsWith(MediaType.APPLICATION_JSON_VALUE)) {
            return false;
        }
        return true;
    }

    private static byte[] normalizeHeader(String header) {
        if (header == null || header.isBlank()) {
            return new byte[0];
        }
        return header.strip().getBytes(StandardCharsets.UTF_8);
    }

    static boolean secretsEqual(byte[] configured, byte[] provided) {
        if (provided.length != configured.length) {
            MessageDigest.isEqual(configured, configured);
            return false;
        }
        return MessageDigest.isEqual(configured, provided);
    }
}
