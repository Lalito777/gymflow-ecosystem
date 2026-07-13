package cl.joaedu.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UserRequest(

        @NotBlank(message = "name es obligatorio")
        String name,

        @NotBlank(message = "email es obligatorio")
        @Email(message = "email debe tener un formato valido")
        String email,

        @NotBlank(message = "subscriptionPlan es obligatorio")
        String subscriptionPlan,

        @NotBlank(message = "password es obligatorio")
        @Size(min = 6, message = "password debe tener al menos 6 caracteres")
        String password,

        @NotBlank(message = "role es obligatorio")
        String role,

        @NotNull(message = "branchId es obligatorio")
        @Positive(message = "branchId debe ser un numero positivo")
        Long branchId
) {}
