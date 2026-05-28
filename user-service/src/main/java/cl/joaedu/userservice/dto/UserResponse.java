package cl.joaedu.userservice.dto;

public record UserResponse(
        Long id,
        String name,
        String email,
        String subscriptionPlan,
        String role
) {}