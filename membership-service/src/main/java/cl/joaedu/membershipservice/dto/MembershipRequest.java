package cl.joaedu.membershipservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de entrada para crear una membresia.
 * No se usa la entidad Membership directamente: el cliente solo entrega los IDs,
 * las fechas y el estado los calcula el servicio (fechaVencimiento = hoy + duracionDias del plan).
 */
public record MembershipRequest(

        @NotNull(message = "userId es obligatorio")
        @Positive(message = "userId debe ser un numero positivo")
        Long userId,

        @NotNull(message = "planId es obligatorio")
        @Positive(message = "planId debe ser un numero positivo")
        Long planId
) {}
