package cl.joaedu.membershipservice.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato uniforme de error para todo membership-service.
 * fields queda null cuando el error no viene de una validacion de campos (Bean Validation).
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fields
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> fields) {
        this(LocalDateTime.now(), status, error, message, path, fields);
    }
}
