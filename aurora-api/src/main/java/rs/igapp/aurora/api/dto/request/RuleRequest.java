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
public class RuleRequest {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String condition;  // Detection logic

    private Long statusId;

    private Long defaultSeverityId;

    private Boolean enabled;

    private String alertMessage;
}