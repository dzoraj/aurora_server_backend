package rs.igapp.aurora.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import rs.igapp.aurora.domain.entity.AlertStatus;

@Repository
public interface AlertStatusRepository extends JpaRepository<AlertStatus, Long> {

    Optional<AlertStatus> findByName(String name);
}
