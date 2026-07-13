package cl.joaedu.accessservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GenerateTokenRequest(

        @NotNull(message = "userId es obligatorio")
        @Positive(message = "userId debe ser un numero positivo")
        Long userId,

        @NotNull(message = "branchId es obligatorio")
        @Positive(message = "branchId debe ser un numero positivo")
        Long branchId
) {}
