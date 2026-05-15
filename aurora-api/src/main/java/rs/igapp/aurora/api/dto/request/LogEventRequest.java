package rs.igapp.aurora.api.dto.request;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEventRequest {

    @NotNull
    private Long sourceId;

    @NotBlank
    private String message;

    private Long severityId;

    private String rawData;  // JSON as string

    /**
     * Optional event time. Accepts ISO-8601 with offset or Z (e.g. from {@code new Date().toISOString()} in the browser).
     * Stored as {@code LocalDateTime} using the date-time fields from this value (same wall clock as in the payload).
     */
    private OffsetDateTime timestamp;
}
