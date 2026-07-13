package cl.joaedu.classservice.dto;

/**
 * DTO propio de class-service para el dato remoto que entrega membership-service.
 * Se consume via RestClient (no Feign) para cumplir con ambas formas de comunicacion
 * exigidas por la pauta.
 */
public record MembershipStatusDto(
        Long userId,
        boolean activa
) {}
