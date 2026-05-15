package rs.igapp.aurora.server.detection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class LogCommittedDetectionListener {

    private static final Logger log = LoggerFactory.getLogger(LogCommittedDetectionListener.class);

    private final DetectionEngine detectionEngine;

    public LogCommittedDetectionListener(DetectionEngine detectionEngine) {
        this.detectionEngine = detectionEngine;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLogCreated(LogEventCreatedEvent event) {
        try {
            detectionEngine.evaluateCommittedLog(event.logEventId());
        } catch (RuntimeException ex) {
            log.error("Detection failed after log commit; logEventId={}", event.logEventId(), ex);
        }
    }
}
