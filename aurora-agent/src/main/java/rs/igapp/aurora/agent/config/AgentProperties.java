package rs.igapp.aurora.agent.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aurora.agent")
public class AgentProperties {

    /** Base URL of Aurora server! */
    private String apiBaseUrl = "http://localhost:8080";

    /** Fixed sourceID for testing*/
    private long sourceId = 1;

    private AgentMode mode = AgentMode.FILE;


    private Path filePath;


    private long pollIntervalMs = 2000;


    private Long severityId;


    private String messagePrefix = "";


    private String ingestApiKey = "";

    private final OAuth oauth = new OAuth();

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public long getSourceId() {
        return sourceId;
    }

    public void setSourceId(long sourceId) {
        this.sourceId = sourceId;
    }

    public AgentMode getMode() {
        return mode;
    }

    public void setMode(AgentMode mode) {
        this.mode = mode;
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public long getPollIntervalMs() {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(long pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }

    public Long getSeverityId() {
        return severityId;
    }

    public void setSeverityId(Long severityId) {
        this.severityId = severityId;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public void setMessagePrefix(String messagePrefix) {
        this.messagePrefix = messagePrefix;
    }

    public String getIngestApiKey() {
        return ingestApiKey;
    }

    public void setIngestApiKey(String ingestApiKey) {
        this.ingestApiKey = ingestApiKey;
    }

    public OAuth getOauth() {
        return oauth;
    }

    public static class OAuth {

        private String tokenUri = "";

        private String clientId = "";

        private String clientSecret = "";

        private String username = "";

        private String password = "";

        private String accessToken = "";

        public String getTokenUri() {
            return tokenUri;
        }

        public void setTokenUri(String tokenUri) {
            this.tokenUri = tokenUri;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
