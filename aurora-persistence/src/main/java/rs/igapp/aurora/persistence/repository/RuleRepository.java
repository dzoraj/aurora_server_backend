package rs.igapp.aurora.persistence.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.igapp.aurora.domain.entity.Rule;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {
    
    @Query("SELECT r FROM Rule r WHERE r.enabled = :enabled AND r.isDeleted = false")
    List<Rule> findByEnabled(@Param("enabled") Boolean enabled);
    
    @Query("SELECT r FROM Rule r WHERE r.status.id = :statusId AND r.isDeleted = false")
    Page<Rule> findByStatusId(@Param("statusId") Long statusId, Pageable pageable);
    
    @Query("SELECT r FROM Rule r WHERE r.name LIKE %:name% AND r.isDeleted = false")
    Page<Rule> searchByName(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT r FROM Rule r WHERE r.enabled = :enabled AND r.status.id = :statusId AND r.isDeleted = false")
    List<Rule> findByEnabledAndStatusId(@Param("enabled") Boolean enabled, @Param("statusId") Long statusId);
}
