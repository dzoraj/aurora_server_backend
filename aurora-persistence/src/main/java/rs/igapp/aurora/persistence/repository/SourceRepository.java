package rs.igapp.aurora.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.igapp.aurora.domain.entity.Source;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
    
    @Query("SELECT s FROM Source s WHERE s.agentId = :agentId AND s.isDeleted = false")
    Optional<Source> findByAgentId(@Param("agentId") String agentId);
    
    @Query("SELECT s FROM Source s WHERE s.isActive = :isActive AND s.isDeleted = false")
    List<Source> findByIsActive(@Param("isActive") Boolean isActive);
    
    @Query("SELECT s FROM Source s WHERE s.hostname = :hostname AND s.isDeleted = false")
    Page<Source> findByHostname(@Param("hostname") String hostname, Pageable pageable);
    
    @Query("SELECT s FROM Source s WHERE s.ipAddress = :ipAddress AND s.isDeleted = false")
    Optional<Source> findByIpAddress(@Param("ipAddress") String ipAddress);
}
