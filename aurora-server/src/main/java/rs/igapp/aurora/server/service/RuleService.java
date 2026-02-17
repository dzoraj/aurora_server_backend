package rs.igapp.aurora.server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.igapp.aurora.api.dto.request.RuleRequest;
import rs.igapp.aurora.api.dto.response.RuleResponse;
import rs.igapp.aurora.domain.entity.Rule;
import rs.igapp.aurora.domain.entity.RuleStatus;
import rs.igapp.aurora.domain.entity.Severity;
import rs.igapp.aurora.persistence.repository.RuleRepository;
import rs.igapp.aurora.persistence.repository.RuleStatusRepository;
import rs.igapp.aurora.persistence.repository.SeverityRepository;

/**
 * RuleService - Upravljanje poslovnom logikom za "detekciona" pravila u SIEM sistemu
 * 
 * SVRHA:
 * Ovaj servis uprlavlja sigurnosnim detekcionim pravilima koje analiziraju log dogadjaje i generisu uzbune
 * Pravila definisu obrazce ili uslove koje daju indikatore na sumnjive aktivnosti (npr. neuspesan pokusaj ulogovanja, nedozvoljeni pristup, znakovi malvera)
 * 
 * KLJUCNE KARAKERISTIKE:
 * - Kreacija, citanje, azuriranje, brisanje
 * - Ukljucivanje/iskljucivanje pravila bez brisanja
 * - Pretraga pravila po imenu ili statusu
 * - Filtriranje aktivnih pravila za mehainzma za detekciju (detection engine)
 * - Azuriranje stanja pravila i sigurnsnog nivoa
 * 
 * pRIMER PRAVILA:
 * Name: "Multiple Failed Logins"
 * Condition: "message contains 'failed login' AND count > 5 in 10 minutes"
 * Severity: HIGH
 * Status: ACTIVE
 * Enabled: true
 */
@Service
public class RuleService extends CrudService<Rule, RuleRequest, RuleResponse, Long> {

    private final RuleRepository ruleRepository;
    private final RuleStatusRepository ruleStatusRepository;
    private final SeverityRepository severityRepository;

    public RuleService(RuleRepository ruleRepository,
                      RuleStatusRepository ruleStatusRepository,
                      SeverityRepository severityRepository) {
        super(ruleRepository);
        this.ruleRepository = ruleRepository;
        this.ruleStatusRepository = ruleStatusRepository;
        this.severityRepository = severityRepository;
    }

    // ==================== CUSTOM QUERY METHODS ====================

    // Dobavi sva pravila koja imaju status enabled

    @Transactional(readOnly = true)
    public List<RuleResponse> getByEnabled(Boolean enabled) {
        return ruleRepository.findByEnabled(enabled).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // Dobavi pravila po statusu (ACTIVE, INACTIVE, ARCHIVED)
   
    @Transactional(readOnly = true)
    public Page<RuleResponse> getByStatus(Long statusId, Pageable pageable) {
        return ruleRepository.findByStatusId(statusId, pageable)
            .map(this::mapToResponse);
    }

    // Pretraga pravila po imenu (delimicno poklapanje, ignorisanje velikih/malih slova)
    @Transactional(readOnly = true)
    public Page<RuleResponse> searchByName(String name, Pageable pageable) {
        return ruleRepository.searchByName(name, pageable)
            .map(this::mapToResponse);
    }

    // Dobaviti aktivna i ukljucena pravila za mehanizam detekcije
    @Transactional(readOnly = true)
    public List<RuleResponse> getActiveEnabledRules(Long statusId) {
        return ruleRepository.findByEnabledAndStatusId(true, statusId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // Ukljuci pravilo (brzo ukljucivanje/iskljucivanje pravila bez potpunog azuriranja)
    
    @Transactional
    public RuleResponse toggleEnabled(Long ruleId, Boolean enabled) {
        return ruleRepository.findById(ruleId)
            .map(rule -> {
                rule.setEnabled(enabled);
                Rule updated = ruleRepository.save(rule);
                return mapToResponse(updated);
            })
            .orElseThrow(() -> new RuntimeException("Rule not found: " + ruleId));
    }

    // ==================== ABSTRACT METHOD IMPLEMENTATIONS ====================

    @Override
    protected Rule mapToEntity(RuleRequest request) {
    	// Pronadji RuleStatus entitet
        RuleStatus ruleStatus = request.getStatusId() != null
            ? ruleStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new RuntimeException("RuleStatus not found: " + request.getStatusId()))
            : null;

        // Pronadji Severity entitet
        Severity severity = request.getDefaultSeverityId() != null
            ? severityRepository.findById(request.getDefaultSeverityId())
                .orElseThrow(() -> new RuntimeException("Severity not found: " + request.getDefaultSeverityId()))
            : null;

        return Rule.builder()
            .name(request.getName())
            .description(request.getDescription())
            .condition(request.getCondition())
            .status(ruleStatus)
            .defaultSeverity(severity)
            .enabled(request.getEnabled() != null ? request.getEnabled() : true)  // Podrazumevano na ENABLED
            .alertMessage(request.getAlertMessage())
            .build();
    }

    // Pretvoriti iz baze podataka entitet na API odgovor (za operacije citanja)

    @Override
    protected RuleResponse mapToResponse(Rule rule) {
        return RuleResponse.builder()
            .id(rule.getId())
            .name(rule.getName())
            .description(rule.getDescription())
            .condition(rule.getCondition())
            .status(rule.getStatus() != null ? rule.getStatus().getName() : null)
            .defaultSeverity(rule.getDefaultSeverity() != null ? rule.getDefaultSeverity().getName() : null)
            .enabled(rule.getEnabled())
            .alertMessage(rule.getAlertMessage())
            .createdAt(rule.getCreatedAt())
            .updatedAt(rule.getUpdatedAt())
            .build();
    }

    // Azurirati postojeci entitet na nove podatke iz API zahteva (za UPDATE operacije)

    @Override
    protected void updateEntity(Rule entity, RuleRequest request) {

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setCondition(request.getCondition());
        entity.setAlertMessage(request.getAlertMessage());


        if (request.getEnabled() != null) {
            entity.setEnabled(request.getEnabled());
        }


        if (request.getStatusId() != null) {
            RuleStatus ruleStatus = ruleStatusRepository.findById(request.getStatusId())
                .orElse(entity.getStatus());
            entity.setStatus(ruleStatus);
        }


        if (request.getDefaultSeverityId() != null) {
            Severity severity = severityRepository.findById(request.getDefaultSeverityId())
                .orElse(entity.getDefaultSeverity());
            entity.setDefaultSeverity(severity);
        }


    }
    
    // SOFT DELETE
    @Override
    @Transactional
    public void delete(Long id) {
        ruleRepository.findById(id).ifPresent(rule -> {
            rule.setIsDeleted(true);
            rule.setDeletedAt(LocalDateTime.now());
            ruleRepository.save(rule);
        });
    }

}
