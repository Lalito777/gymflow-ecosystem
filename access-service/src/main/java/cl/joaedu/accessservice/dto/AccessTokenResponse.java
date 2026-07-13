package cl.joaedu.accessservice.dto;

import java.time.LocalDateTime;

public record AccessTokenResponse(
        Long id,
        Long userId,
        String qrCode,
        LocalDateTime fechaExpiracion,
        String estado
) {}
