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

    List<Rule> findByEnabled(Boolean enabled);

    Page<Rule> findByStatus_Id(Long statusId, Pageable pageable);

    @Query("SELECT r FROM Rule r WHERE r.name LIKE %:name%")
    Page<Rule> searchByName(@Param("name") String name, Pageable pageable);

    List<Rule> findByEnabledAndStatus_Id(Boolean enabled, Long statusId);
}
