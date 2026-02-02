package rs.igapp.aurora.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sources")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String agentId;  // Unique identifier for agent

    @NotBlank
    @Column(nullable = false)
    private String hostname;  // Computer name

    @Column
    private String ipAddress;

    @Column
    private String osType;  // Windows, Linux, macOS

    @Column
    private String agentVersion;

    @Column(nullable = false)
    private Boolean isActive;  // Is agent running?

    @Column
    private LocalDateTime lastHeartbeat;  // Last ping from agent

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // SOFT DELETE FIELDS
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column
    private LocalDateTime deletedAt;

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
