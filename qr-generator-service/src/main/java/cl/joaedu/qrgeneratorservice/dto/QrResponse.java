package cl.joaedu.qrgeneratorservice.dto;

import java.time.LocalDateTime;

public record QrResponse(
        Long id,
        Long accessTokenId,
        String contenidoQr,
        String imagenBase64,
        LocalDateTime fechaCreacion,
        Boolean vigente
) {}
