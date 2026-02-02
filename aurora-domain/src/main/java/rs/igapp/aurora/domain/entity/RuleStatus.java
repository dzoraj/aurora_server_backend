package rs.igapp.aurora.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rule_statuses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;  // ACTIVE, INACTIVE, ARCHIVED

    @Column
    private String description;

    // SOFT DELETE FIELDS
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column
    private LocalDateTime deletedAt;
}
