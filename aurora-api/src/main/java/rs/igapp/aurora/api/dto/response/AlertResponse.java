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
public class AlertResponse {

    private Long id;

    private String ruleName;

    private String sourceId;

    private String severity;

    private String status;

    private String message;

    private String assignedTo;

    private String investigationNotes;

    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;
}
