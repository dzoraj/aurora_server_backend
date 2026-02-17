package rs.igapp.aurora.server.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.igapp.aurora.server.service.CrudService;

/**
 * Apstraktni CRUD kontroler - Generic REST API endpoints za sve entitete
 * 
 * SVRHA:
 * Ovaj apstraktni kontroler pruza klasicne operacije Kreiraj, citaj, azuriraj, obrisi.....
 * ENDPOINT-i koji su pruzeni:
 * - POST   /api/{resource}        -> Create new entity
 * - GET    /api/{resource}/{id}   -> Get entity by ID
 * - GET    /api/{resource}        -> Get paginated list of entities
 * - PUT    /api/{resource}/{id}   -> Update existing entity
 * - DELETE /api/{resource}/{id}   -> Delete entity
 * 
 */
public abstract class CrudController<Request, Response, ID> {

	// Servisni sloj koji obavlja poslovnu logiku
    protected final CrudService<?, Request, Response, ID> service;

    protected CrudController(CrudService<?, Request, Response, ID> service) {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<Response> create(@Valid @RequestBody Request request) {
        Response created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Response> getById(@PathVariable ID id) {
        Response response = service.getById(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<Page<Response>> getAll(Pageable pageable) {
        Page<Response> page = service.getAll(pageable);
        return ResponseEntity.ok(page);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Response> update(@PathVariable ID id, 
                                          @Valid @RequestBody Request request) {
        Response updated = service.update(id, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
