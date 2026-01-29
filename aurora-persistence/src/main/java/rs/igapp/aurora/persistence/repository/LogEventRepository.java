package rs.igapp.aurora.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import rs.igapp.aurora.domain.entity.LogEvent;

@Repository
public interface LogEventRepository extends JpaRepository<LogEvent, Long> {

    Page<LogEvent> findBySourceId(String sourceId, Pageable pageable);

    Page<LogEvent> findBySeverity_Id(Long severityId, Pageable pageable);

    @Query("SELECT l FROM LogEvent l WHERE l.timestamp >= :startTime AND l.timestamp <= :endTime")
    List<LogEvent> findByTimestampRange(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    @Query("SELECT l FROM LogEvent l WHERE l.message LIKE %:keyword%")
    Page<LogEvent> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countBySeverity_Id(Long severityId);
}
