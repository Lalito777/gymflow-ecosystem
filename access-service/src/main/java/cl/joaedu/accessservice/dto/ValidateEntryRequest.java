package cl.joaedu.accessservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ValidateEntryRequest(

        @NotBlank(message = "qrCode es obligatorio")
        String qrCode,

        @NotNull(message = "branchId es obligatorio")
        @Positive(message = "branchId debe ser un numero positivo")
        Long branchId
) {}
