package cl.joaedu.branchservice.dto;

public record BranchRequest(
        String name,
        String address,
        Integer maxCapacity
) {}