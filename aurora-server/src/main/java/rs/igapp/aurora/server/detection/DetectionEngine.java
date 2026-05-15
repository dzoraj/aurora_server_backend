package rs.igapp.aurora.server.detection;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import rs.igapp.aurora.domain.entity.Alert;
import rs.igapp.aurora.domain.entity.AlertStatus;
import rs.igapp.aurora.domain.entity.LogEvent;
import rs.igapp.aurora.domain.entity.Rule;
import rs.igapp.aurora.persistence.repository.AlertRepository;
import rs.igapp.aurora.persistence.repository.AlertStatusRepository;
import rs.igapp.aurora.persistence.repository.LogEventRepository;
import rs.igapp.aurora.persistence.repository.RuleRepository;

/**
 * Runs enabled detection rules against a persisted {@link LogEvent} and creates {@link Alert} rows for matches.
 */
@Service
public class DetectionEngine {

    private static final Logger log = LoggerFactory.getLogger(DetectionEngine.class);

    private static final String NEW_ALERT_STATUS = "NEW";

    private final RuleRepository ruleRepository;
    private final LogEventRepository logEventRepository;
    private final AlertRepository alertRepository;
    private final AlertStatusRepository alertStatusRepository;
    private final RuleConditionEvaluator ruleConditionEvaluator;

    @Value("${aurora.detection.enabled:true}")
    private boolean detectionEnabled;

    public DetectionEngine(
            RuleRepository ruleRepository,
            LogEventRepository logEventRepository,
            AlertRepository alertRepository,
            AlertStatusRepository alertStatusRepository,
            RuleConditionEvaluator ruleConditionEvaluator) {
        this.ruleRepository = ruleRepository;
        this.logEventRepository = logEventRepository;
        this.alertRepository = alertRepository;
        this.alertStatusRepository = alertStatusRepository;
        this.ruleConditionEvaluator = ruleConditionEvaluator;
    }

    /**
     * Runs detection in a new transaction after the ingest transaction has committed so failures here do not
     * roll back or fail the HTTP response for log creation.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void evaluateCommittedLog(Long logEventId) {
        if (!detectionEnabled || logEventId == null) {
            return;
        }
        LogEvent event = logEventRepository.findById(logEventId).orElse(null);
        if (event == null) {
            log.warn("Log event {} not found for detection", logEventId);
            return;
        }
        AlertStatus newStatus =
                alertStatusRepository
                        .findByName(NEW_ALERT_STATUS)
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "AlertStatus '" + NEW_ALERT_STATUS + "' not found; seed reference data"));

        List<Rule> rules = ruleRepository.findAllActiveForDetection();
        for (Rule rule : rules) {
            try {
                evaluateRule(rule, event, newStatus);
            } catch (RuntimeException ex) {
                log.warn("Detection skipped for rule id={} logEvent id={}: {}", rule.getId(), event.getId(), ex.getMessage());
            }
        }
    }

    private void evaluateRule(Rule rule, LogEvent event, AlertStatus newStatus) {
        if (rule.getDefaultSeverity() == null) {
            log.warn("Rule id={} has no defaultSeverity; skipping", rule.getId());
            return;
        }
        if (!ruleConditionEvaluator.matches(rule, event)) {
            return;
        }
        if (alertRepository.countByRuleAndTriggeringLogEvent(rule.getId(), event.getId()) > 0) {
            return;
        }
        String alertText = renderAlertMessage(rule, event);
        Alert alert =
                Alert.builder()
                        .rule(rule)
                        .triggeringLogEvent(event)
                        .source(event.getSource())
                        .severity(rule.getDefaultSeverity())
                        .status(newStatus)
                        .message(alertText)
                        .build();
        alertRepository.save(alert);
    }

    private String renderAlertMessage(Rule rule, LogEvent event) {
        String template = rule.getAlertMessage();
        if (template == null || template.isBlank()) {
            return "Rule matched: " + rule.getName();
        }
        String hostname = "";
        String agentId = "";
        if (event.getSource() != null) {
            hostname = nullToEmpty(event.getSource().getHostname());
            agentId = nullToEmpty(event.getSource().getAgentId());
        }
        return template.replace("{message}", nullToEmpty(event.getMessage()))
                .replace("{hostname}", hostname)
                .replace("{agentId}", agentId);
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}
