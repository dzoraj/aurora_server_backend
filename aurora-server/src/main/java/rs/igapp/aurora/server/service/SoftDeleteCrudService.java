package rs.igapp.aurora.server.service;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import rs.igapp.aurora.domain.entity.SoftDeletable;
import rs.igapp.aurora.persistence.repository.SoftDeleteRepository;

@Transactional
public abstract class SoftDeleteCrudService<
        Entity extends SoftDeletable,
        Request,
        Response,
        ID extends Serializable>
        extends CrudService<Entity, Request, Response, ID> {

    protected final SoftDeleteRepository<Entity, ID> softDeleteRepository;

    protected SoftDeleteCrudService(SoftDeleteRepository<Entity, ID> repository) {
        super(repository);
        this.softDeleteRepository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Response getById(ID id) {
        return softDeleteRepository.findByIdAndIsDeletedFalse(id)
            .map(this::mapToResponse)
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Response> getAll(Pageable pageable) {
        return softDeleteRepository.findAllByIsDeletedFalse(pageable)
            .map(this::mapToResponse);
    }

    @Override
    public Response update(ID id, Request request) {
        return softDeleteRepository.findByIdAndIsDeletedFalse(id)
            .map(entity -> {
                updateEntity(entity, request);
                Entity updated = softDeleteRepository.save(entity);
                return mapToResponse(updated);
            })
            .orElse(null);
    }

    @Override
    public void delete(ID id) {
        softDeleteRepository.findByIdAndIsDeletedFalse(id).ifPresent(entity -> {
            entity.setIsDeleted(true);
            entity.setDeletedAt(LocalDateTime.now());
            softDeleteRepository.save(entity);
        });
    }
}