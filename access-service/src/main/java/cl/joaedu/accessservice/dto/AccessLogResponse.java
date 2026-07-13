package cl.joaedu.accessservice.dto;

import java.time.LocalDateTime;

public record AccessLogResponse(
        Long id,
        Long userId,
        Long branchId,
        String tipo,
        LocalDateTime timestamp
) {}
