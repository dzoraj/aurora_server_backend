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
public class IncidentRequest {

    @NotBlank
    private String title;

    private String description;

    private Long severityId;

    private Long statusId;

    private String assignedTo;

    private String timeline;
}
