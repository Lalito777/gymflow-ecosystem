package cl.joaedu.notificationservice.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long destinatarioId,
        String tipo,
        String mensaje,
        LocalDateTime creadoEn
) {}
