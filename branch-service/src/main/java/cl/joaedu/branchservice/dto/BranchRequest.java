package cl.joaedu.branchservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BranchRequest(

        @NotBlank(message = "name es obligatorio")
        String name,

        @NotBlank(message = "address es obligatorio")
        String address,

        @NotNull(message = "maxCapacity es obligatorio")
        @Positive(message = "maxCapacity debe ser un numero positivo")
        Integer maxCapacity
) {}
