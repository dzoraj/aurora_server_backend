package rs.igapp.aurora.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRequest {

    @NotNull
    private Long ruleId;

    @NotNull
    private Long logEventId;

    @NotNull
    private Long sourceId;

    @NotNull
    private Long severityId;

    @NotNull
    private Long statusId;

    private String message;

    private String assignedTo;

    private String investigationNotes;
}
