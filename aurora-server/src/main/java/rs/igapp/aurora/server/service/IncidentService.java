package rs.igapp.aurora.server.service;

import rs.igapp.aurora.api.dto.request.IncidentRequest;
import rs.igapp.aurora.api.dto.response.IncidentResponse;
import rs.igapp.aurora.domain.entity.Alert;
import rs.igapp.aurora.domain.entity.AlertStatus;
import rs.igapp.aurora.domain.entity.Incident;
import rs.igapp.aurora.domain.entity.Severity;
import rs.igapp.aurora.persistence.repository.AlertRepository;
import rs.igapp.aurora.persistence.repository.AlertStatusRepository;
import rs.igapp.aurora.persistence.repository.IncidentRepository;
import rs.igapp.aurora.persistence.repository.SeverityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**SRB
 * IncidentService - Poslovna logika za menadzment sigurnosnih incidenata u SIEM sistemu
 * 
 * SVRHA:
 * Ovaj servis upravlja sigurnosnim incidentima koja su kolekcija povezanih uzbuna koje predstavljaju koordinisan napad ili neki sigurnosno-povezan dogadjaj kojem je potrebna istraga
 * 
 * INCIDEN vs UZBUNA:
 * - UZBUNA: jedna detekcija (npr. neuspeli pokusaj ulogovanja)
 * - INCIDENT: Grupa povezanih uzuna (npr. Brute force napad koji sadrzi 10 neuspelih pokusaja ulogovanja + 2 puta skeniranje portova + malver)
 * 
 * ZIVOTNI CIKLUS INCIDENTA:
 * 1. NEW - Incident napravljen od strane analiticara
 * 2. INVESTIGATING - Tim aktivno istrazuje
 * 3. RESOLVED - Incident "obradjen" i zatvoren
 * 4. FALSE_POSITIVE - "Lazna uzbuna" tj. lazni incident
 * 
 * KLJUCNE KARAKTERISTIKE:
 * - Kreiranje incidenata rucno ili iz vise uzbuna
 * - Grupno povezane uzbune pod jednim incidentom
 * - Dodaj/Ukloni uzbune iz incidenta
 * - Pratiti tok incidenta vremenski i stanje istrage
 * - Filtriranje po statusu, ozbiljnosti i zaduzenom analiticaru
 * - Generisanje izvestaja incidenta
 * 
 * PRRIMER TOKA RADA:
 * Analiticar vidi 5 uzbuna iz iste IP adrese da napada razlicite sisteme -> analiticar kreira incident "NAPADI SA 192.168.0.100" ->
 * povezuje sve uzbune na ovaj incident -> Istrazuje -> razresava incident (sve uzbune povezane su isto resene)
 */
/**ENG - GOOGLE TRANSLATE WITHOUT CORRECTIONS
* IncidentService - Business logic for security incident management in SIEM system
*
* PURPOSE:
* This service manages security incidents which are a collection of related alerts that represent a coordinated attack or some security-related event that needs investigation
*
* INCIDENT vs ALARM:
* - ALARM: a single detection (e.g. failed login attempt)
* - INCIDENT: A group of related alerts (e.g. Brute force attack containing 10 failed login attempts + 2 times port scanning + malware)
*
* INCIDENT LIFECYCLE:
* 1. NEW - Incident created by an analyst
* 2. INVESTIGATING - The team is actively investigating
* 3. RESOLVED - Incident "processed" and closed
* 4. FALSE_POSITIVE - "False alert" i.e. fake incident
*
* KEY FEATURES:
* - Create incidents manually or from multiple alerts
* - Group related alerts under one incident
* - Add/Remove alerts from an incident
* - Track incident progress over time and investigation status
* - Filter by status, severity and assigned analyst
* - Generate incident reports
*
* EXAMPLE WORKFLOW:
* Analyst sees 5 alerts from the same IP address attacking different systems -> analyst creates incident "ATTACK FROM 192.168.0.100" ->
* links all alerts to this incident -> Investigates -> resolves incident (all related alerts are resolved as well)
*/
@Service
public class IncidentService extends CrudService<Incident, IncidentRequest, IncidentResponse, Long> {

    private final IncidentRepository incidentRepository;
    private final SeverityRepository severityRepository;
    private final AlertStatusRepository alertStatusRepository;
    private final AlertRepository alertRepository;


    public IncidentService(IncidentRepository incidentRepository,
                          SeverityRepository severityRepository,
                          AlertStatusRepository alertStatusRepository,
                          AlertRepository alertRepository) {
        super(incidentRepository);
        this.incidentRepository = incidentRepository;
        this.severityRepository = severityRepository;
        this.alertStatusRepository = alertStatusRepository;
        this.alertRepository = alertRepository;
    }

    // ==================== CUSTOM QUERY METHODS ====================

    // Get incidents by status (NEW, INVESTIGATING, RESOLVED, FALSE_POSITIVE)

    //Dashboard showing all "NEW" incidents awaiting assignment

    @Transactional(readOnly = true)
    public Page<IncidentResponse> getByStatus(Long statusId, Pageable pageable) {
        return incidentRepository.findByStatus_Id(statusId, pageable)
            .map(this::mapToResponse);
    }

    // Get incidents by severity level (CRITICAL, HIGH, MEDIUM, LOW, INFO)
    // Filter to show only CRITICAL incidents for immediate attention
     
    @Transactional(readOnly = true)
    public Page<IncidentResponse> getBySeverity(Long severityId, Pageable pageable) {
        return incidentRepository.findBySeverity_Id(severityId, pageable)
            .map(this::mapToResponse);
    }

    // Get incidents assigned to specific analyst
    // Analyst logs in and sees their assigned incidents
    
    @Transactional(readOnly = true)
    public Page<IncidentResponse> getByAssignedTo(String assignedTo, Pageable pageable) {
        return incidentRepository.findByAssignedTo(assignedTo, pageable)
            .map(this::mapToResponse);
    }

    // Get incidents created within a time range
    // Generate report of all incidents from last week
     
    @Transactional(readOnly = true)
    public List<IncidentResponse> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return incidentRepository.findByCreatedAtRange(startTime, endTime).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    // Get all open (unresolved) incidents
    // Dashboard widget: "15 open incidents"
    @Transactional(readOnly = true)
    public List<IncidentResponse> getOpenIncidents() {
        return incidentRepository.findOpenIncidents().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // Count incidents by status (for dashboard metrics)
     
    @Transactional(readOnly = true)
    public long countByStatus(Long statusId) {
        return incidentRepository.countByStatus_Id(statusId);
    }

    // ==================== INCIDENT-ALERT RELATIONSHIP METHODS ====================

    // Add an alert to an incident (many-to-many relationship)
     
    @Transactional
    public IncidentResponse addAlertToIncident(Long incidentId, Long alertId) {
        Incident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));
        
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        
        // Add alert to incident's alert set (many-to-many)
        incident.getAlerts().add(alert);
        
        Incident updated = incidentRepository.save(incident);
        return mapToResponse(updated);
    }

    // Remove an alert from an incident
     
    @Transactional
    public IncidentResponse removeAlertFromIncident(Long incidentId, Long alertId) {
        Incident incident = incidentRepository.findById(incidentId)
            .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));
        
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        
        // Remove alert from incident's alert set
        incident.getAlerts().remove(alert);
        
        Incident updated = incidentRepository.save(incident);
        return mapToResponse(updated);
    }

    // Create incident from multiple existing alerts
     
    @Transactional
    public IncidentResponse createIncidentWithAlerts(IncidentRequest request, Set<Long> alertIds) {
        // Create base incident
        Incident incident = mapToEntity(request);
        
        // Fetch and link all alerts
        if (alertIds != null && !alertIds.isEmpty()) {
            Set<Alert> alerts = new HashSet<>();
            for (Long alertId : alertIds) {
                Alert alert = alertRepository.findById(alertId)
                    .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
                alerts.add(alert);
            }
            incident.setAlerts(alerts);
        }
        
        Incident saved = incidentRepository.save(incident);
        return mapToResponse(saved);
    }

    // Get all alerts linked to an incident
     
    @Transactional(readOnly = true)
    public Set<Long> getIncidentAlerts(Long incidentId) {
        return incidentRepository.findById(incidentId)
            .map(incident -> incident.getAlerts().stream()
                .map(Alert::getId)
                .collect(Collectors.toSet()))
            .orElse(new HashSet<>());
    }

    // ==================== INCIDENT LIFECYCLE METHODS ====================

    // Assign incident to analyst for investigation
    
    @Transactional
    public IncidentResponse assignToAnalyst(Long incidentId, String analystUsername, Long investigatingStatusId) {
        return incidentRepository.findById(incidentId)
            .map(incident -> {
                incident.setAssignedTo(analystUsername);
                
                // Change status to INVESTIGATING
                AlertStatus investigatingStatus = alertStatusRepository.findById(investigatingStatusId)
                    .orElseThrow(() -> new RuntimeException("Status not found: " + investigatingStatusId));
                incident.setStatus(investigatingStatus);
                
                Incident updated = incidentRepository.save(incident);
                return mapToResponse(updated);
            })
            .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));
    }

    // Update incident timeline (investigation progress notes)
    
    @Transactional
    public IncidentResponse updateTimeline(Long incidentId, String timeline) {
        return incidentRepository.findById(incidentId)
            .map(incident -> {
                incident.setTimeline(timeline);
                Incident updated = incidentRepository.save(incident);
                return mapToResponse(updated);
            })
            .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));
    }

    // Resolve incident (mark as handled and closed)
     
    @Transactional
    public IncidentResponse resolveIncident(Long incidentId, Long resolvedStatusId) {
        return incidentRepository.findById(incidentId)
            .map(incident -> {
                AlertStatus resolvedStatus = alertStatusRepository.findById(resolvedStatusId)
                    .orElseThrow(() -> new RuntimeException("Status not found: " + resolvedStatusId));
                
                incident.setStatus(resolvedStatus);
                incident.setResolvedAt(LocalDateTime.now());
                
                Incident updated = incidentRepository.save(incident);
                return mapToResponse(updated);
            })
            .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));
    }

    //* Mark incident as false positive
     
    @Transactional
    public IncidentResponse markAsFalsePositive(Long incidentId, Long falsePositiveStatusId) {
        return incidentRepository.findById(incidentId)
            .map(incident -> {
                AlertStatus fpStatus = alertStatusRepository.findById(falsePositiveStatusId)
                    .orElseThrow(() -> new RuntimeException("Status not found: " + falsePositiveStatusId));
                
                incident.setStatus(fpStatus);
                incident.setResolvedAt(LocalDateTime.now());
                
                Incident updated = incidentRepository.save(incident);
                return mapToResponse(updated);
            })
            .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));
    }

    // ==================== ABSTRACT METHOD IMPLEMENTATIONS ====================

    /**
     * Convert API request to database entity (for CREATE operation)
     * 
     * PROCESS:
     * 1. Look up Severity entity by ID
     * 2. Look up AlertStatus entity by ID (incidents reuse alert statuses)
     * 3. Build Incident entity with all fields
     * 4. Initialize empty alerts set (alerts linked separately via addAlertToIncident)
     * 
     */
    @Override
    protected Incident mapToEntity(IncidentRequest request) {
        // Find related entities
        Severity severity = request.getSeverityId() != null
            ? severityRepository.findById(request.getSeverityId())
                .orElseThrow(() -> new RuntimeException("Severity not found: " + request.getSeverityId()))
            : null;
        
        AlertStatus status = request.getStatusId() != null
            ? alertStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new RuntimeException("Status not found: " + request.getStatusId()))
            : null;

        return Incident.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .severity(severity)
            .status(status)
            .assignedTo(request.getAssignedTo())
            .timeline(request.getTimeline())
            .alerts(new HashSet<>())  // Initialize empty set
            .build();
    }

    /**
     * Convert database entity to API response (for READ operations)
     * 
     * SIMPLIFICATION:
     * - Severity entity -> severity name string
     * - AlertStatus entity -> status name string
     * - Alert entities -> Set of alert IDs only (not full alert details)
     */
    @Override
    protected IncidentResponse mapToResponse(Incident incident) {
        // Extract alert IDs from ManyToMany relationship
        Set<Long> alertIds = incident.getAlerts() != null
            ? incident.getAlerts().stream()
                .map(Alert::getId)
                .collect(Collectors.toSet())
            : new HashSet<>();

        return IncidentResponse.builder()
            .id(incident.getId())
            .title(incident.getTitle())
            .description(incident.getDescription())
            .severity(incident.getSeverity() != null ? incident.getSeverity().getName() : null)
            .status(incident.getStatus() != null ? incident.getStatus().getName() : null)
            .assignedTo(incident.getAssignedTo())
            .timeline(incident.getTimeline())
            .alertIds(alertIds)  // Set of alert IDs
            .createdAt(incident.getCreatedAt())
            .resolvedAt(incident.getResolvedAt())
            .build();
    }

    /**
     * Update existing entity with new data from API request (for UPDATE operation)
     * 
     * BUSINESS RULES:
     * - Update all provided fields
     * - Look up new related entities if IDs changed
     * - Preserve existing relationships if request fields are null
     * - Do NOT modify alerts set here (use addAlert/removeAlert methods)
     * 
     */
    @Override
    protected void updateEntity(Incident entity, IncidentRequest request) {
        // Update basic fields
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setAssignedTo(request.getAssignedTo());
        entity.setTimeline(request.getTimeline());

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

        // alerts set is NOT updated here
        // Use addAlertToIncident() or removeAlertFromIncident() for that

    }
}
