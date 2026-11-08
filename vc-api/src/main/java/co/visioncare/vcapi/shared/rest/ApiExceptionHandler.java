package co.visioncare.vcapi.shared.rest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import co.visioncare.vcapi.shared.exceptions.BusinessRuleException;
import co.visioncare.vcapi.shared.exceptions.ConflictException;
import co.visioncare.vcapi.shared.exceptions.ResourceNotFoundException;
import co.visioncare.vcapi.shared.exceptions.UnauthorizedException;

/**
 * Translates domain and validation errors into the shared error shape from
 * vc-docs/07-api/rest-conventions.md: {@code { error, message, details? }}.
 * 400 malformed or invalid request, 401 bad credentials, 404 missing resource,
 * 409 conflict with the current state, 422 business rule.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> businessRule(BusinessRuleException ex) {
        return body(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", ex.getMessage(), null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> conflict(ConflictException ex) {
        return body(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integrity(DataIntegrityViolationException ex) {
        return body(HttpStatus.CONFLICT, "CONFLICT", "The request conflicts with existing data", null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> unauthorized(UnauthorizedException ex) {
        return body(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), null);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> malformed(Exception ex) {
        return body(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Malformed request", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> {
                    Map<String, String> d = new LinkedHashMap<>();
                    d.put("field", fe.getField());
                    d.put("message", fe.getDefaultMessage());
                    return d;
                }).toList();
        return body(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid request", details);
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String code, String message, Object details) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("error", code);
        payload.put("message", message);
        if (details != null) {
            payload.put("details", details);
        }
        return ResponseEntity.status(status).body(payload);
    }
}
