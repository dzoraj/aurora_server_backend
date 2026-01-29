package rs.igapp.aurora.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {

    private Long id;

    private String title;

    private String description;

    private String severity;

    private String status;

    private String assignedTo;

    private String timeline;

    private Set<Long> alertIds;

    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;
}
