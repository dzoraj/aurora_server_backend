package rs.igapp.aurora.domain.entity;

import java.time.LocalDateTime;

import javax.print.attribute.standard.Severity;
import javax.xml.transform.Source;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "log_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @ManyToOne
    @JoinColumn(name = "severity_id")
    private Severity severity;

    @Column(columnDefinition = "JSONB")
    private String rawData;  // Extra metadata as JSON

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
