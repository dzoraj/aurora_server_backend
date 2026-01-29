package rs.igapp.aurora.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import rs.igapp.aurora.domain.entity.Severity;

@Repository
public interface SeverityRepository extends JpaRepository<Severity, Long> {

    Optional<Severity> findByName(String name);

    Optional<Severity> findByLevel(Integer level);
}
