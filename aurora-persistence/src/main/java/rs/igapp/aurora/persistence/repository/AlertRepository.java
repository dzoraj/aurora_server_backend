package rs.igapp.aurora.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.igapp.aurora.domain.entity.Alert;

@Repository
public interface AlertRepository extends SoftDeleteRepository<Alert, Long> {

    @Query("SELECT a FROM Alert a WHERE a.status.id = :statusId AND a.isDeleted = false")
    Page<Alert> findByStatusId(@Param("statusId") Long statusId, Pageable pageable);

    @Query("SELECT a FROM Alert a WHERE a.severity.id = :severityId AND a.isDeleted = false")
    Page<Alert> findBySeverityId(@Param("severityId") Long severityId, Pageable pageable);

    @Query("SELECT a FROM Alert a WHERE a.rule.id = :ruleId AND a.isDeleted = false")
    Page<Alert> findByRuleId(@Param("ruleId") Long ruleId, Pageable pageable);

    @Query("SELECT a FROM Alert a WHERE a.createdAt BETWEEN :startTime AND :endTime AND a.isDeleted = false")
    List<Alert> findByCreatedAtRange(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.status.id = :statusId AND a.isDeleted = false")
    long countByStatusId(@Param("statusId") Long statusId);

    @Query("""
        SELECT a FROM Alert a
        WHERE a.assignedTo = :analyst
          AND a.status.id <> :resolvedStatusId
          AND a.isDeleted = false
    """)
    List<Alert> findOpenAlertsByAnalyst(@Param("analyst") String analyst,
                                        @Param("resolvedStatusId") Long resolvedStatusId);

    @Query(
            """
            SELECT COUNT(a) FROM Alert a
            WHERE a.rule.id = :ruleId
              AND a.triggeringLogEvent.id = :logEventId
              AND a.isDeleted = false
            """)
    long countByRuleAndTriggeringLogEvent(@Param("ruleId") Long ruleId, @Param("logEventId") Long logEventId);
}