package rs.igapp.aurora.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceResponse {

    private Long id;

    private String agentId;

    private String hostname;

    private String ipAddress;

    private String osType;

    private String agentVersion;

    private Boolean isActive;

    private LocalDateTime lastHeartbeat;

    private LocalDateTime createdAt;
}
