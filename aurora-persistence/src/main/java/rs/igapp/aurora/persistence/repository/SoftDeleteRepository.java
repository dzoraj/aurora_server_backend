package rs.igapp.aurora.persistence.repository;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import rs.igapp.aurora.domain.entity.SoftDeletable;

@NoRepositoryBean
public interface SoftDeleteRepository<Entity extends SoftDeletable, ID extends Serializable>
        extends JpaRepository<Entity, ID> {

    Optional<Entity> findByIdAndIsDeletedFalse(ID id);

    Page<Entity> findAllByIsDeletedFalse(Pageable pageable);
}