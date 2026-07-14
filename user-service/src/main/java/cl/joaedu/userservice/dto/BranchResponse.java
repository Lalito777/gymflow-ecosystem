package cl.joaedu.userservice.dto;

public record BranchResponse(
        Long id,
        String name,
        String address,
        Integer maxCapacity
) {}