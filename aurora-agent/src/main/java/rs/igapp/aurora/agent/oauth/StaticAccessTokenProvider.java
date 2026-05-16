package rs.igapp.aurora.agent.oauth;

import java.util.Objects;

import org.springframework.util.StringUtils;

public final class StaticAccessTokenProvider implements AccessTokenProvider {

    private final String token;

    public StaticAccessTokenProvider(String token) {
        this.token = Objects.requireNonNull(StringUtils.trimWhitespace(token), "access token");
    }

    @Override
    public String getAccessToken() {
        return token;
    }
}
