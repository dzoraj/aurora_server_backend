package rs.igapp.aurora.api.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEventRequest {

    @NotBlank
    private String sourceId;

    @NotBlank
    private String message;

    private Long severityId;

    private String rawData;  // JSON as string

    private LocalDateTime timestamp;
}
