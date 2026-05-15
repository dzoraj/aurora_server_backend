package rs.igapp.aurora.server.detection;

/**
 * Published after a {@link rs.igapp.aurora.domain.entity.LogEvent} row is committed so detection can run
 * in a separate transaction without failing the ingest HTTP request.
 */
public record LogEventCreatedEvent(Long logEventId) {}
