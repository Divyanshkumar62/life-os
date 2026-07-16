package com.lifeos.system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Missing/unknown resource (e.g. nonexistent player id) -> 404 Not Found.
     * Services signal this by throwing IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(IllegalArgumentException e) {
        return buildResponse(e, HttpStatus.NOT_FOUND);
    }

    /**
     * Malformed path/request argument (e.g. a non-UUID {playerId}) -> 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return buildResponse(e, HttpStatus.BAD_REQUEST);
    }

    /**
     * Illegal state transition (wrong OnboardingStage or unmet timer precondition) -> 409 Conflict.
     * Services signal this by throwing IllegalStateException.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
        return buildResponse(e, HttpStatus.CONFLICT);
    }

    /**
     * Fallback for unclassified failures -> 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception e) {
        return buildResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, String>> buildResponse(Exception e, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getClass().getSimpleName());
        response.put("message", e.getMessage());
        return new ResponseEntity<>(response, status);
    }
}
