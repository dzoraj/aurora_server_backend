package rs.igapp.aurora.persistence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.igapp.aurora.domain.entity.Severity;

@Repository
public interface SeverityRepository extends JpaRepository<Severity, Long> {
    
    @Query("SELECT s FROM Severity s WHERE s.name = :name AND s.isDeleted = false")
    Optional<Severity> findByName(@Param("name") String name);
    
    @Query("SELECT s FROM Severity s WHERE s.level = :level AND s.isDeleted = false")
    Optional<Severity> findByLevel(@Param("level") Integer level);
}
