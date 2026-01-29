package rs.igapp.aurora.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import rs.igapp.aurora.domain.entity.Alert;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findByStatus_Id(Long statusId, Pageable pageable);

    Page<Alert> findBySeverity_Id(Long severityId, Pageable pageable);

    Page<Alert> findByRule_Id(Long ruleId, Pageable pageable);

    @Query("SELECT a FROM Alert a WHERE a.createdAt >= :startTime AND a.createdAt <= :endTime")
    List<Alert> findByCreatedAtRange(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    long countByStatus_Id(Long statusId);

    @Query("SELECT a FROM Alert a WHERE a.assignedTo = :analyst AND a.status.id != :resolvedStatusId")
    List<Alert> findOpenAlertsByAnalyst(@Param("analyst") String analyst,
                                        @Param("resolvedStatusId") Long resolvedStatusId);
}
