package cl.joaedu.userservice.dto;

public record UserRequest(
        String name,
        String email,
        String subscriptionPlan,
        String password,
        String role
) {}