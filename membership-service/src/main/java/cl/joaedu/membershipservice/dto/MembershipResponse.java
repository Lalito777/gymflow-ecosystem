package cl.joaedu.membershipservice.dto;

import java.time.LocalDate;

public record MembershipResponse(
        Long id,
        Long userId,
        Long planId,
        LocalDate fechaInicio,
        LocalDate fechaVencimiento,
        String estado
) {}
