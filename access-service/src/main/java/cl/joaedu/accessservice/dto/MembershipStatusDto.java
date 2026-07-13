package cl.joaedu.accessservice.dto;

/**
 * DTO propio de access-service para el dato remoto que entrega membership-service
 * en GET /api/membership/status/{userId}. Cada servicio mantiene su propia copia
 * del contrato que consume (no se comparte la clase entre microservicios), tal como
 * exige la pauta para la comunicacion Feign entre dominios.
 */
public record MembershipStatusDto(
        Long userId,
        boolean activa
) {}
