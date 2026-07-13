package cl.joaedu.classservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReserveClassRequest(

        @NotNull(message = "userId es obligatorio")
        @Positive(message = "userId debe ser un numero positivo")
        Long userId,

        @NotNull(message = "classId es obligatorio")
        @Positive(message = "classId debe ser un numero positivo")
        Long classId
) {}
