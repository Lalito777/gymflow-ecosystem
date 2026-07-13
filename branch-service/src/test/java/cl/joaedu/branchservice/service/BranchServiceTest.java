package cl.joaedu.branchservice.service;

import cl.joaedu.branchservice.dto.BranchRequest;
import cl.joaedu.branchservice.dto.BranchResponse;
import cl.joaedu.branchservice.model.Branch;
import cl.joaedu.branchservice.repository.BranchRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private BranchService branchService;

    @Test
    void create_conDatosValidos_deberiaGuardarYRetornarSede() {
        // Given
        BranchRequest request = new BranchRequest("Sede Centro", "Av. Principal 123", 50);
        Branch saved = new Branch("Sede Centro", "Av. Principal 123", 50);
        saved.setId(1L);
        when(branchRepository.save(any(Branch.class))).thenReturn(saved);

        // When
        BranchResponse result = branchService.create(request);

        // Then
        assertEquals(1L, result.id());
        assertEquals("Sede Centro", result.name());
        assertEquals(50, result.maxCapacity());
        verify(branchRepository, times(1)).save(any(Branch.class));
    }

    @Test
    void create_siRepositorioFalla_deberiaPropagarExcepcion() {
        // Given
        BranchRequest request = new BranchRequest("Sede Norte", "Calle 1", 30);
        when(branchRepository.save(any(Branch.class))).thenThrow(new RuntimeException("Error de BD"));

        // When / Then
        assertThrows(RuntimeException.class, () -> branchService.create(request));
    }

    @Test
    void findAll_conSedesRegistradas_deberiaRetornarListaMapeada() {
        // Given
        Branch b1 = new Branch("Sede Centro", "Av. Principal 123", 50);
        b1.setId(1L);
        Branch b2 = new Branch("Sede Sur", "Calle Sur 45", 40);
        b2.setId(2L);
        when(branchRepository.findAll()).thenReturn(List.of(b1, b2));

        // When
        List<BranchResponse> result = branchService.findAll();

        // Then
        assertEquals(2, result.size());
        assertEquals("Sede Centro", result.get(0).name());
    }

    @Test
    void findAll_sinSedes_deberiaRetornarListaVacia() {
        // Given
        when(branchRepository.findAll()).thenReturn(List.of());

        // When
        List<BranchResponse> result = branchService.findAll();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_conSedeExistente_deberiaRetornarla() {
        // Given
        Branch branch = new Branch("Sede Centro", "Av. Principal 123", 50);
        branch.setId(1L);
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));

        // When
        BranchResponse result = branchService.findById(1L);

        // Then
        assertEquals("Sede Centro", result.name());
    }

    @Test
    void findById_conSedeInexistente_deberiaLanzarExcepcion() {
        // Given
        when(branchRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(EntityNotFoundException.class, () -> branchService.findById(99L));
    }
}
