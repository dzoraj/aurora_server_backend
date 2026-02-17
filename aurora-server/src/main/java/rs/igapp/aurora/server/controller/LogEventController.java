package rs.igapp.aurora.server.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.igapp.aurora.api.dto.request.LogEventRequest;
import rs.igapp.aurora.api.dto.response.LogEventResponse;
import rs.igapp.aurora.server.service.LogEventService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ENDPOINTS:
 * - GET /api/log-events/source/{agentId} - Filter by source/agent
 * - GET /api/log-events/severity/{severityId} - Filter by severity
 * - GET /api/log-events/search?keyword=... - Search message content
 * - GET /api/log-events/range?start=...&end=... - Time range filter
 * - GET /api/log-events/count/severity/{severityId} - Severity counts
 *
 */
@RestController
@RequestMapping("/api/log-events")
public class LogEventController extends CrudController<LogEventRequest, LogEventResponse, Long> {

    private final LogEventService logEventService;

    public LogEventController(LogEventService logEventService) {
        super(logEventService);
        this.logEventService = logEventService;
    }

    // FILTERING ENDPOINTS (from LogEventRepository)
    @GetMapping("/source/{agentId}")
    public ResponseEntity<Page<LogEventResponse>> getBySource(
            @PathVariable String agentId,
            Pageable pageable) {
        Page<LogEventResponse> logs = logEventService.getBySource(agentId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/severity/{severityId}")
    public ResponseEntity<Page<LogEventResponse>> getBySeverity(
            @PathVariable Long severityId,
            Pageable pageable) {
        Page<LogEventResponse> logs = logEventService.getBySeverity(severityId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<LogEventResponse>> search(
            @RequestParam String keyword,
            Pageable pageable) {
        Page<LogEventResponse> logs = logEventService.search(keyword, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/range")
    public ResponseEntity<List<LogEventResponse>> getByTimeRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        List<LogEventResponse> logs = logEventService.getByTimeRange(start, end);
        return ResponseEntity.ok(logs);
    }

    // STATISTICS
    @GetMapping("/count/severity/{severityId}")
    public ResponseEntity<Long> countBySeverity(@PathVariable Long severityId) {
        long count = logEventService.countBySeverity(severityId);
        return ResponseEntity.ok(count);
    }
}
