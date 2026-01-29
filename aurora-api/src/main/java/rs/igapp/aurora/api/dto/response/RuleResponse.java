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
public class RuleResponse {

    private Long id;

    private String name;

    private String description;

    private String condition;

    private String status;

    private String defaultSeverity;

    private Boolean enabled;

    private String alertMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
