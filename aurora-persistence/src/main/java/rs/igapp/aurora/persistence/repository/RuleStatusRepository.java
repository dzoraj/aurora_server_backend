package rs.igapp.aurora.persistence.repository;

import rs.igapp.aurora.domain.entity.RuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RuleStatusRepository extends JpaRepository<RuleStatus, Long> {

    Optional<RuleStatus> findByName(String name);
}
