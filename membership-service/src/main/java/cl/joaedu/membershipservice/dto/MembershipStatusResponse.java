package cl.joaedu.membershipservice.dto;

public record MembershipStatusResponse(
        Long userId,
        boolean activa
) {}
