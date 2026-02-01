package rs.igapp.aurora.server.service;

import rs.igapp.aurora.api.dto.request.AlertRequest;
import rs.igapp.aurora.api.dto.response.AlertResponse;
import rs.igapp.aurora.domain.entity.Alert;
import rs.igapp.aurora.domain.entity.AlertStatus;
import rs.igapp.aurora.domain.entity.LogEvent;
import rs.igapp.aurora.domain.entity.Rule;
import rs.igapp.aurora.domain.entity.Severity;
import rs.igapp.aurora.domain.entity.Source;
import rs.igapp.aurora.persistence.repository.AlertRepository;
import rs.igapp.aurora.persistence.repository.AlertStatusRepository;
import rs.igapp.aurora.persistence.repository.LogEventRepository;
import rs.igapp.aurora.persistence.repository.RuleRepository;
import rs.igapp.aurora.persistence.repository.SeverityRepository;
import rs.igapp.aurora.persistence.repository.SourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AlertService - Poslovna logika za upravljanje sigurnosnim uzbunama u SIEM sistemu
 * 
 * SVRHA:
 * Ovaj servis upravlja zivotnim ciklusom sigurnosnih uzbuna koje se generisu kada detekciona pravila
 * se podudaraju sa sumnjivim log eventovima.
 * 
 * 
 * ZIVOTNI CIKLUS UZBUNE:
 * 1. NEW - Uzbuna tek napravljena od strane detekcionog mehanizma
 * 2. INVESTIGATING - Analiticar pregleda uzbunu
 * 3. RESOLVED - Uzbuna obradjena 
 * 4. FALSE_POSITIVE - LAZNA UZBUNA (pravilo treba da bude promenjeno/pregledano)
 * 
 * KLJUCNE KARAKTERISTIKE:
 * - Kreiraj uzbune kada se pravilo okine
 * - Filtriraj uzbunu po statusu, ozbiljnosti, pravilu, vremenskom okviru
 * - Dodeli uzbunu analiticaru
 * - Dodaj istrazne beleske
 * - Razresi uzbunu ili obelezi kao lazna uzbuna
 * - Prati otvorene uzbune po analiticaru
 * 
 * PRIMER TOKA:
 * Pravilo "Previse neuspeli pokusaja ulogovanja" -> Uzbuna napravljena (NEW) -> Analiticar pregleda (INVESTIGATING) -> Shvati da je pokusaj napada -> Resava (RESOLVED)
 */

/** !!QUICK GOOGLE TRANSLATION FROM SERBIAN TO ENGLISH!!
 * AlertService - Business logic for managing security alerts in a SIEM system
 * 
 * PURPOSE:
 * This service manages the lifecycle of security alerts that are generated when detection rules
 * match suspicious log events.
 * 
 * 
 * ALARM LIFE CYCLE:
 * 1. NEW - Alert just created by the detection mechanism
 * 2. INVESTIGATING - The analyst reviews the alert
 * 3. RESOLVED - Alert processed 
 * 4. FALSE_POSITIVE - FALSE ALARM (rule should be changed/reviewed)
 * 
 * KEY FEATURES:
 * - Create alerts when a rule is fired
 * - Filter alert by status, severity, rule, time frame
 * - Assign an alert to the analyst
 * - Add investigative notes
 * - Resolve the alarm or mark it as a false alarm
 * - Track open alerts by analyst
 * 
 * EXAMPLE FLOW:
 * Rule "Too Many Failed Login Attempts" -> Alert Created (NEW) -> Analyst Review (INVESTIGATING) -> Realize Attack Attempt -> Resovles (RESOLVED)
*/

@Service
public class AlertService extends CrudService<Alert, AlertRequest, AlertResponse, Long> {

    private final AlertRepository alertRepository;
    private final RuleRepository ruleRepository;
    private final LogEventRepository logEventRepository;
    private final SourceRepository sourceRepository;
    private final SeverityRepository severityRepository;
    private final AlertStatusRepository alertStatusRepository;


    public AlertService(AlertRepository alertRepository,
                       RuleRepository ruleRepository,
                       LogEventRepository logEventRepository,
                       SourceRepository sourceRepository,
                       SeverityRepository severityRepository,
                       AlertStatusRepository alertStatusRepository) {
        super(alertRepository);
        this.alertRepository = alertRepository;
        this.ruleRepository = ruleRepository;
        this.logEventRepository = logEventRepository;
        this.sourceRepository = sourceRepository;
        this.severityRepository = severityRepository;
        this.alertStatusRepository = alertStatusRepository;
    }


    @Transactional(readOnly = true)
    public Page<AlertResponse> getByStatus(Long statusId, Pageable pageable) {
        return alertRepository.findByStatus_Id(statusId, pageable)
            .map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    public Page<AlertResponse> getBySeverity(Long severityId, Pageable pageable) {
        return alertRepository.findBySeverity_Id(severityId, pageable)
            .map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    public Page<AlertResponse> getByRule(Long ruleId, Pageable pageable) {
        return alertRepository.findByRule_Id(ruleId, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return alertRepository.findByCreatedAtRange(startTime, endTime).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countByStatus(Long statusId) {
        return alertRepository.countByStatus_Id(statusId);
    }


    @Transactional(readOnly = true)
    public List<AlertResponse> getOpenAlertsByAnalyst(String analyst, Long resolvedStatusId) {
        return alertRepository.findOpenAlertsByAnalyst(analyst, resolvedStatusId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }


    @Transactional
    public AlertResponse assignToAnalyst(Long alertId, String analystUsername, Long investigatingStatusId) {
        return alertRepository.findById(alertId)
            .map(alert -> {
                alert.setAssignedTo(analystUsername);
                
                // Change status to INVESTIGATING
                AlertStatus investigatingStatus = alertStatusRepository.findById(investigatingStatusId)
                    .orElseThrow(() -> new RuntimeException("AlertStatus not found: " + investigatingStatusId));
                alert.setStatus(investigatingStatus);
                
                Alert updated = alertRepository.save(alert);
                return mapToResponse(updated);
            })
            .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
    }

    //Add investigation notes to an alert

    @Transactional
    public AlertResponse addInvestigationNotes(Long alertId, String notes) {
        return alertRepository.findById(alertId)
            .map(alert -> {
                alert.setInvestigationNotes(notes);
                Alert updated = alertRepository.save(alert);
                return mapToResponse(updated);
            })
            .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
    }

    // Resolve an alert (mark as handled)

    @Transactional
    public AlertResponse resolveAlert(Long alertId, Long resolvedStatusId) {
        return alertRepository.findById(alertId)
            .map(alert -> {
                AlertStatus resolvedStatus = alertStatusRepository.findById(resolvedStatusId)
                    .orElseThrow(() -> new RuntimeException("AlertStatus not found: " + resolvedStatusId));
                
                alert.setStatus(resolvedStatus);
                alert.setResolvedAt(LocalDateTime.now());
                
                Alert updated = alertRepository.save(alert);
                return mapToResponse(updated);
            })
            .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
    }

    // Mark alert as false positive (incorrect detection)

    @Transactional
    public AlertResponse markAsFalsePositive(Long alertId, Long falsePositiveStatusId) {
        return alertRepository.findById(alertId)
            .map(alert -> {
                AlertStatus fpStatus = alertStatusRepository.findById(falsePositiveStatusId)
                    .orElseThrow(() -> new RuntimeException("AlertStatus not found: " + falsePositiveStatusId));
                
                alert.setStatus(fpStatus);
                alert.setResolvedAt(LocalDateTime.now());
                
                Alert updated = alertRepository.save(alert);
                return mapToResponse(updated);
            })
            .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
    }


    @Override
    protected Alert mapToEntity(AlertRequest request) {
        // Find related entities (all required)
        Rule rule = ruleRepository.findById(request.getRuleId())
            .orElseThrow(() -> new RuntimeException("Rule not found: " + request.getRuleId()));
        
        LogEvent logEvent = logEventRepository.findById(request.getLogEventId())
            .orElseThrow(() -> new RuntimeException("LogEvent not found: " + request.getLogEventId()));
        
        Source source = sourceRepository.findById(request.getSourceId())
            .orElseThrow(() -> new RuntimeException("Source not found: " + request.getSourceId()));
        
        Severity severity = severityRepository.findById(request.getSeverityId())
            .orElseThrow(() -> new RuntimeException("Severity not found: " + request.getSeverityId()));
        
        AlertStatus status = alertStatusRepository.findById(request.getStatusId())
            .orElseThrow(() -> new RuntimeException("AlertStatus not found: " + request.getStatusId()));

        return Alert.builder()
            .rule(rule)
            .triggeringLogEvent(logEvent)
            .source(source)
            .severity(severity)
            .status(status)
            .message(request.getMessage())
            .assignedTo(request.getAssignedTo())
            .investigationNotes(request.getInvestigationNotes())
            .build();
    }

    // Convert database entity to API response (for READ operations)

    @Override
    protected AlertResponse mapToResponse(Alert alert) {
        return AlertResponse.builder()
            .id(alert.getId())
            .ruleName(alert.getRule() != null ? alert.getRule().getName() : null)
            .sourceId(alert.getSource() != null ? alert.getSource().getAgentId() : null)
            .severity(alert.getSeverity() != null ? alert.getSeverity().getName() : null)
            .status(alert.getStatus() != null ? alert.getStatus().getName() : null)
            .message(alert.getMessage())
            .assignedTo(alert.getAssignedTo())
            .investigationNotes(alert.getInvestigationNotes())
            .createdAt(alert.getCreatedAt())
            .resolvedAt(alert.getResolvedAt())
            .build();
    }

    // Update existing entity with new data from API request (for UPDATE operation)

    @Override
    protected void updateEntity(Alert entity, AlertRequest request) {
        // Update basic fields
        entity.setMessage(request.getMessage());
        entity.setAssignedTo(request.getAssignedTo());
        entity.setInvestigationNotes(request.getInvestigationNotes());

        // Update Rule if provided
        if (request.getRuleId() != null) {
            Rule rule = ruleRepository.findById(request.getRuleId())
                .orElse(entity.getRule());
            entity.setRule(rule);
        }

        // Update LogEvent if provided
        if (request.getLogEventId() != null) {
            LogEvent logEvent = logEventRepository.findById(request.getLogEventId())
                .orElse(entity.getTriggeringLogEvent());
            entity.setTriggeringLogEvent(logEvent);
        }

        // Update Source if provided
        if (request.getSourceId() != null) {
            Source source = sourceRepository.findById(request.getSourceId())
                .orElse(entity.getSource());
            entity.setSource(source);
        }

        // Update Severity if provided
        if (request.getSeverityId() != null) {
            Severity severity = severityRepository.findById(request.getSeverityId())
                .orElse(entity.getSeverity());
            entity.setSeverity(severity);
        }

        // Update AlertStatus if provided
        if (request.getStatusId() != null) {
            AlertStatus status = alertStatusRepository.findById(request.getStatusId())
                .orElse(entity.getStatus());
            entity.setStatus(status);
        }

    }
}
