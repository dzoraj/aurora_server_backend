package rs.igapp.aurora.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import rs.igapp.aurora.domain.entity.Incident;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Page<Incident> findByStatus_Id(Long statusId, Pageable pageable);

    Page<Incident> findBySeverity_Id(Long severityId, Pageable pageable);

    Page<Incident> findByAssignedTo(String assignedTo, Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE i.createdAt >= :startTime AND i.createdAt <= :endTime")
    List<Incident> findByCreatedAtRange(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    @Query("SELECT i FROM Incident i WHERE i.resolvedAt IS NULL")
    List<Incident> findOpenIncidents();

    long countByStatus_Id(Long statusId);
}
