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
public class LogEventResponse {

    private Long id;

    private String sourceId;

    private String message;

    private String severity;

    private String rawData;

    private LocalDateTime timestamp;

    private LocalDateTime createdAt;
}
