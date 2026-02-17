package rs.igapp.aurora.server.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.igapp.aurora.api.dto.request.IncidentRequest;
import rs.igapp.aurora.api.dto.response.IncidentResponse;
import rs.igapp.aurora.server.service.IncidentService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ENDPOINTS:
 * - GET /api/incidents/status/{statusId} - Filter incidents by status
 * - GET /api/incidents/severity/{severityId} - Filter by severity
 * - GET /api/incidents/assigned/{assignedTo} - Filter by assigned analyst
 * - GET /api/incidents/range?start=...&end=... - Incidents in time range
 * - GET /api/incidents/open - All open/unresolved incidents
 * - PATCH /api/incidents/{id}/assign?analyst=...&investigatingStatusId=... - Assign to analyst
 * - PATCH /api/incidents/{id}/timeline - Update timeline (body: string)
 * - PATCH /api/incidents/{id}/resolve?resolvedStatusId=... - Mark resolved
 * - PATCH /api/incidents/{id}/false-positive?falsePositiveStatusId=... - Mark false positive
 * - PATCH /api/incidents/{id}/alerts/{alertId} - Add alert to incident
 * - DELETE /api/incidents/{id}/alerts/{alertId} - Remove alert from incident
 * - GET /api/incidents/count/status/{statusId} - Count by status
 */
@RestController
@RequestMapping("/api/incidents")
public class IncidentController extends CrudController<IncidentRequest, IncidentResponse, Long> {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        super(incidentService);
        this.incidentService = incidentService;
    }

    // FILTERING ENDPOINTS
    @GetMapping("/status/{statusId}")
    public ResponseEntity<Page<IncidentResponse>> getByStatus(
            @PathVariable Long statusId,
            Pageable pageable) {
        Page<IncidentResponse> incidents = incidentService.getByStatus(statusId, pageable);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/severity/{severityId}")
    public ResponseEntity<Page<IncidentResponse>> getBySeverity(
            @PathVariable Long severityId,
            Pageable pageable) {
        Page<IncidentResponse> incidents = incidentService.getBySeverity(severityId, pageable);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/assigned/{assignedTo}")
    public ResponseEntity<Page<IncidentResponse>> getByAssignedTo(
            @PathVariable String assignedTo,
            Pageable pageable) {
        Page<IncidentResponse> incidents = incidentService.getByAssignedTo(assignedTo, pageable);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/range")
    public ResponseEntity<List<IncidentResponse>> getByTimeRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        List<IncidentResponse> incidents = incidentService.getByTimeRange(start, end);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/open")
    public ResponseEntity<List<IncidentResponse>> getOpenIncidents() {
        List<IncidentResponse> incidents = incidentService.getOpenIncidents();
        return ResponseEntity.ok(incidents);
    }

    // LIFECYCLE MANAGEMENT
    @PatchMapping("/{id}/assign")
    public ResponseEntity<IncidentResponse> assign(
            @PathVariable Long id,
            @RequestParam String analyst,
            @RequestParam Long investigatingStatusId) {
        IncidentResponse updated = incidentService.assignToAnalyst(id, analyst, investigatingStatusId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/timeline")
    public ResponseEntity<IncidentResponse> updateTimeline(
            @PathVariable Long id,
            @RequestBody String timeline) {
        IncidentResponse updated = incidentService.updateTimeline(id, timeline);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<IncidentResponse> resolve(
            @PathVariable Long id,
            @RequestParam Long resolvedStatusId) {
        IncidentResponse updated = incidentService.resolveIncident(id, resolvedStatusId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/false-positive")
    public ResponseEntity<IncidentResponse> markAsFalsePositive(
            @PathVariable Long id,
            @RequestParam Long falsePositiveStatusId) {
        IncidentResponse updated = incidentService.markAsFalsePositive(id, falsePositiveStatusId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/alerts/{alertId}")
    public ResponseEntity<IncidentResponse> addAlert(
            @PathVariable Long id,
            @PathVariable Long alertId) {
        IncidentResponse updated = incidentService.addAlertToIncident(id, alertId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}/alerts/{alertId}")
    public ResponseEntity<IncidentResponse> removeAlert(
            @PathVariable Long id,
            @PathVariable Long alertId) {
        IncidentResponse updated = incidentService.removeAlertFromIncident(id, alertId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @GetMapping("/count/status/{statusId}")
    public ResponseEntity<Long> countByStatus(@PathVariable Long statusId) {
        long count = incidentService.countByStatus(statusId);
        return ResponseEntity.ok(count);
    }
}
