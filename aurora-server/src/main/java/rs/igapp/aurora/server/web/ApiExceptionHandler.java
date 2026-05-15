package rs.igapp.aurora.server.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Returns JSON error bodies so API clients can see why a request failed.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Validation failed");
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        Throwable root = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause() : ex;
        log.warn("Malformed request body: {}", root.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Malformed request body");
        body.put("detail", root.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        Throwable root = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause() : ex;
        log.warn("Data integrity violation: {}", root.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Request could not be applied");
        body.put("detail", root.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", ex.getStatusCode().value());
        body.put("message", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAny(Exception ex) {
        ResponseStatusException rse = findCauseOfType(ex, ResponseStatusException.class);
        if (rse != null) {
            return handleResponseStatus(rse);
        }
        log.error("Unhandled API exception", ex);
        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 500);
        body.put("message", ex.getMessage());
        body.put("detail", root.getMessage());
        body.put("exception", ex.getClass().getSimpleName());
        return ResponseEntity.status(500).body(body);
    }

    private static <T extends Throwable> T findCauseOfType(Throwable ex, Class<T> type) {
        Throwable c = ex;
        while (c != null) {
            if (type.isInstance(c)) {
                return type.cast(c);
            }
            c = c.getCause();
        }
        return null;
    }
}
