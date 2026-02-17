package rs.igapp.aurora.server.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class CrudService<Entity, Request, Response, ID> {

    protected final JpaRepository<Entity, ID> repository;

    protected CrudService(JpaRepository<Entity, ID> repository) {
        this.repository = repository;
    }

    // CREATE
    public Response create(Request request) {
        Entity entity = mapToEntity(request);
        Entity saved = repository.save(entity);
        return mapToResponse(saved);
    }

    // READ
    @Transactional(readOnly = true)
    public Response getById(ID id) {
        return repository.findById(id)
            .map(this::mapToResponse)
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<Response> getAll(Pageable pageable) {
        return repository.findAll(pageable)
            .map(this::mapToResponse);
    }

    // UPDATE
    public Response update(ID id, Request request) {
        return repository.findById(id)
            .map(entity -> {
                updateEntity(entity, request);
                Entity updated = repository.save(entity);
                return mapToResponse(updated);
            })
            .orElse(null);
    }

    // DELETE
    public void delete(ID id) {
        repository.deleteById(id);  
    }


    protected abstract Entity mapToEntity(Request request);
    
    protected abstract Response mapToResponse(Entity entity);
    
    protected abstract void updateEntity(Entity entity, Request request);
}
