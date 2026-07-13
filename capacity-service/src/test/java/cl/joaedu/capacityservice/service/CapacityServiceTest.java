package cl.joaedu.capacityservice.service;

import cl.joaedu.capacityservice.dto.CapacityResponse;
import cl.joaedu.capacityservice.model.CapacityCounter;
import cl.joaedu.capacityservice.repository.CapacityCounterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapacityServiceTest {

    @Mock
    private CapacityCounterRepository repo;

    @InjectMocks
    private CapacityService capacityService;

    @Test
    void increment_sinContadorPrevio_deberiaCrearloConUnaPersona() {
        // Given
        when(repo.findByBranchId(1L)).thenReturn(Optional.empty());
        when(repo.save(any(CapacityCounter.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        CapacityResponse result = capacityService.increment(1L);

        // Then
        assertEquals(1, result.personasActuales());
        assertEquals(100, result.capacidadMaxima());
        assertEquals(1.0, result.porcentajeOcupacion());
        verify(repo, times(1)).save(any(CapacityCounter.class));
    }

    @Test
    void increment_conContadorExistente_deberiaSumarUnaPersonaYRecalcularPorcentaje() {
        // Given
        CapacityCounter existente = new CapacityCounter(1L, 24, 100, 24.0, LocalDateTime.now());
        when(repo.findByBranchId(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(CapacityCounter.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        CapacityResponse result = capacityService.increment(1L);

        // Then
        assertEquals(25, result.personasActuales());
        assertEquals(25.0, result.porcentajeOcupacion());
    }

    @Test
    void decrement_conPersonasEnElAforo_deberiaRestarUna() {
        // Given
        CapacityCounter existente = new CapacityCounter(1L, 10, 100, 10.0, LocalDateTime.now());
        when(repo.findByBranchId(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(CapacityCounter.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        CapacityResponse result = capacityService.decrement(1L);

        // Then
        assertEquals(9, result.personasActuales());
        assertEquals(9.0, result.porcentajeOcupacion());
    }

    @Test
    void decrement_conAforoEnCero_noDeberiaQuedarNegativo() {
        // Given: regla de negocio clave -> el aforo nunca puede bajar de 0
        CapacityCounter existente = new CapacityCounter(1L, 0, 100, 0.0, LocalDateTime.now());
        when(repo.findByBranchId(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(CapacityCounter.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        CapacityResponse result = capacityService.decrement(1L);

        // Then
        assertEquals(0, result.personasActuales());
        assertEquals(0.0, result.porcentajeOcupacion());
    }
}
