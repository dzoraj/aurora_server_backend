package rs.igapp.aurora.server.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.igapp.aurora.api.dto.request.RuleRequest;
import rs.igapp.aurora.api.dto.response.RuleResponse;
import rs.igapp.aurora.server.service.RuleService;

import java.util.List;

/**
 * ENDPOINTS:
 * - GET /api/rules/active - All enabled/active detection rules (for engine)
 * - GET /api/rules/status/{statusId} - Filter by status (ACTIVE/INACTIVE/ARCHIVED)
 * - GET /api/rules/search?name=... - Search rules by name
 * - PATCH /api/rules/{id}/toggle?enabled=true - Quick enable/disable
 *
 */
@RestController
@RequestMapping("/api/rules")
public class RuleController extends CrudController<RuleRequest, RuleResponse, Long> {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        super(ruleService);
        this.ruleService = ruleService;
    }

    // FILTERING ENDPOINTS (from RuleService/RuleRepository)
    @GetMapping("/active")
    public ResponseEntity<List<RuleResponse>> getActiveRules(@RequestParam(required = false) Long statusId) {
        List<RuleResponse> rules = ruleService.getActiveEnabledRules(statusId);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/status/{statusId}")
    public ResponseEntity<Page<RuleResponse>> getByStatus(
            @PathVariable Long statusId,
            Pageable pageable) {
        Page<RuleResponse> rules = ruleService.getByStatus(statusId, pageable);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<RuleResponse>> search(
            @RequestParam String name,
            Pageable pageable) {
        Page<RuleResponse> rules = ruleService.searchByName(name, pageable);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/enabled/{enabled}")
    public ResponseEntity<List<RuleResponse>> getByEnabled(
            @PathVariable Boolean enabled) {
        List<RuleResponse> rules = ruleService.getByEnabled(enabled);
        return ResponseEntity.ok(rules);
    }

    // RULE LIFECYCLE
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RuleResponse> toggleEnabled(
            @PathVariable Long id,
            @RequestParam Boolean enabled) {
        RuleResponse updated = ruleService.toggleEnabled(id, enabled);
        return ResponseEntity.ok(updated);
    }
}
