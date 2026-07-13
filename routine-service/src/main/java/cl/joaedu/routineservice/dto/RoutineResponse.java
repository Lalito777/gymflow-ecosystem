package cl.joaedu.routineservice.dto;

import java.time.LocalDate;

public record RoutineResponse(
        Long id,
        Long userId,
        String nombre,
        String objetivo,
        String creadoPor,
        LocalDate fechaCreacion
) {}
