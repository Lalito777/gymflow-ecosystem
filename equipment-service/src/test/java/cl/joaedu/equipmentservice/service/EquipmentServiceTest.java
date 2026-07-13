package cl.joaedu.equipmentservice.service;

import cl.joaedu.equipmentservice.dto.EquipmentRequest;
import cl.joaedu.equipmentservice.dto.EquipmentResponse;
import cl.joaedu.equipmentservice.model.Equipment;
import cl.joaedu.equipmentservice.repository.EquipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository repository;

    @InjectMocks
    private EquipmentService equipmentService;

    @Test
    void create_conEstadoValido_deberiaGuardarElEquipo() {
        // Given
        EquipmentRequest request = new EquipmentRequest("Trotadora", "Cardio", 1L, "DISPONIBLE");
        when(repository.save(any(Equipment.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        EquipmentResponse result = equipmentService.create(request);

        // Then
        assertEquals("Trotadora", result.nombre());
        assertEquals("DISPONIBLE", result.estado());
        verify(repository, times(1)).save(any(Equipment.class));
    }

    @Test
    void create_conEstadoInvalido_deberiaLanzarExcepcion() {
        // Given: regla de negocio -> el estado debe ser uno de los valores del dominio
        EquipmentRequest request = new EquipmentRequest("Trotadora", "Cardio", 1L, "ROTO");

        // When / Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> equipmentService.create(request));
        assertTrue(ex.getMessage().contains("Estado invalido"));
        verify(repository, never()).save(any());
    }

    @Test
    void getByBranch_deberiaMapearLosEquiposDeLaSucursal() {
        // Given
        Equipment eq = new Equipment("Barra olimpica", "Fuerza", 2L, "DISPONIBLE");
        when(repository.findByBranchId(2L)).thenReturn(List.of(eq));

        // When
        List<EquipmentResponse> result = equipmentService.getByBranch(2L);

        // Then
        assertEquals(1, result.size());
        assertEquals("Barra olimpica", result.get(0).nombre());
    }
}
