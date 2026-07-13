package cl.joaedu.qrgeneratorservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GenerateQrRequest(

        @NotNull(message = "accessTokenId es obligatorio")
        @Positive(message = "accessTokenId debe ser un numero positivo")
        Long accessTokenId,

        @NotBlank(message = "contenido es obligatorio")
        String contenido
) {}
