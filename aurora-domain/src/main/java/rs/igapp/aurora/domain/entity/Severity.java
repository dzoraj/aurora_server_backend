package rs.igapp.aurora.domain.entity;

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
@Table(name = "severities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Severity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;  // CRITICAL, HIGH, MEDIUM, LOW, INFO

    @Column
    private Integer level;  // 5, 4, 3, 2, 1 (for sorting)

    @Column
    private String description;
}
