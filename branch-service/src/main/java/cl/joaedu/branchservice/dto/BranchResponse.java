package cl.joaedu.branchservice.dto;

public record BranchResponse(
        Long id,
        String name,
        String address,
        Integer maxCapacity
) {}