package rs.igapp.aurora.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;

    @ManyToOne
    @JoinColumn(name = "log_event_id", nullable = false)
    private LogEvent triggeringLogEvent;

    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @ManyToOne
    @JoinColumn(name = "severity_id", nullable = false)
    private Severity severity;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private AlertStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column
    private String assignedTo;  // Analyst username

    @Column(columnDefinition = "TEXT")
    private String investigationNotes;

    @Column
    private LocalDateTime resolvedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
