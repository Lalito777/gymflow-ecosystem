package cl.joaedu.equipmentservice.dto;

public record EquipmentResponse(
        Long id,
        String nombre,
        String categoria,
        Long branchId,
        String estado
) {}
