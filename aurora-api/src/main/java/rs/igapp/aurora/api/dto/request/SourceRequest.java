package rs.igapp.aurora.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceRequest {

    @NotBlank
    private String agentId;

    @NotBlank
    private String hostname;

    private String ipAddress;

    private String osType;

    private String agentVersion;

    private Boolean isActive;
}
