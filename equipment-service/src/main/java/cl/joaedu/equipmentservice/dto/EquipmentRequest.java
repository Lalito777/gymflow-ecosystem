package cl.joaedu.equipmentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EquipmentRequest(

        @NotBlank(message = "nombre es obligatorio")
        String nombre,

        @NotBlank(message = "categoria es obligatoria")
        String categoria,

        @NotNull(message = "branchId es obligatorio")
        @Positive(message = "branchId debe ser un numero positivo")
        Long branchId,

        @NotBlank(message = "estado es obligatorio")
        String estado
) {}
