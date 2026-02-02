package rs.igapp.aurora.persistence.repository;

import rs.igapp.aurora.domain.entity.RuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RuleStatusRepository extends JpaRepository<RuleStatus, Long> {
    
    @Query("SELECT r FROM RuleStatus r WHERE r.name = :name AND r.isDeleted = false")
    Optional<RuleStatus> findByName(@Param("name") String name);
}
