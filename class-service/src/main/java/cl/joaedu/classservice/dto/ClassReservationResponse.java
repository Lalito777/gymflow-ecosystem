package cl.joaedu.classservice.dto;

import java.time.LocalDateTime;

public record ClassReservationResponse(
        Long id,
        Long userId,
        Long classId,
        LocalDateTime fechaReserva,
        String estado
) {}
