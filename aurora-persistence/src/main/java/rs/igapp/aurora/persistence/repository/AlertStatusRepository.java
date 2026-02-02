package rs.igapp.aurora.persistence.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.igapp.aurora.domain.entity.AlertStatus;

@Repository
public interface AlertStatusRepository extends JpaRepository<AlertStatus, Long> {
    
    @Query("SELECT a FROM AlertStatus a WHERE a.name = :name AND a.isDeleted = false")
    Optional<AlertStatus> findByName(@Param("name") String name);
}
