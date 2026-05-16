package rs.igapp.aurora.agent.oauth;

@FunctionalInterface
public interface AccessTokenProvider {

    String getAccessToken();
}
