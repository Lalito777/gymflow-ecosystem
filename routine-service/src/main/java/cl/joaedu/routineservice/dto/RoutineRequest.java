package cl.joaedu.routineservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RoutineRequest(

        @NotNull(message = "userId es obligatorio")
        @Positive(message = "userId debe ser un numero positivo")
        Long userId,

        @NotBlank(message = "nombre es obligatorio")
        @Size(max = 100, message = "nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 150, message = "objetivo no puede superar 150 caracteres")
        String objetivo,

        @NotBlank(message = "creadoPor es obligatorio")
        String creadoPor
) {}
