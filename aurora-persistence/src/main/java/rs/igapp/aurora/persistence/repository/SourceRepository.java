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

    Optional<Source> findByAgentId(String agentId);

    List<Source> findByIsActive(Boolean isActive);

    Page<Source> findByHostname(String hostname, Pageable pageable);

    @Query("SELECT s FROM Source s WHERE s.ipAddress = :ipAddress")
    Optional<Source> findByIpAddress(@Param("ipAddress") String ipAddress);
}
