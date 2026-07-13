package cl.joaedu.capacityservice.dto;

import java.time.LocalDateTime;

public record CapacityResponse(
        Long id,
        Long branchId,
        Integer personasActuales,
        Integer capacidadMaxima,
        Double porcentajeOcupacion,
        LocalDateTime ultimaActualizacion
) {}
