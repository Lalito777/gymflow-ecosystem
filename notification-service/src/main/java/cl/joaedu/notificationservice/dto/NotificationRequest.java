package cl.joaedu.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record NotificationRequest(

        @NotNull(message = "destinatarioId es obligatorio")
        @Positive(message = "destinatarioId debe ser un numero positivo")
        Long destinatarioId,

        @NotBlank(message = "tipo es obligatorio")
        String tipo,

        @NotBlank(message = "mensaje es obligatorio")
        @Size(max = 500, message = "mensaje no puede superar 500 caracteres")
        String mensaje
) {}
