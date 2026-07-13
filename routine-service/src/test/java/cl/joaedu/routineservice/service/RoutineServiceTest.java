package cl.joaedu.routineservice.service;

import cl.joaedu.routineservice.dto.RoutineRequest;
import cl.joaedu.routineservice.dto.RoutineResponse;
import cl.joaedu.routineservice.model.Routine;
import cl.joaedu.routineservice.repository.RoutineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutineServiceTest {

    @Mock
    private RoutineRepository repository;

    @InjectMocks
    private RoutineService routineService;

    @Test
    void createRoutine_conDatosValidos_deberiaGuardarConFechaDeHoy() {
        // Given
        RoutineRequest request = new RoutineRequest(1L, "Fuerza tren superior", "Hipertrofia", "coach.ana");
        when(repository.save(any(Routine.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        RoutineResponse result = routineService.createRoutine(request);

        // Then
        assertEquals("Fuerza tren superior", result.nombre());
        assertEquals(LocalDate.now(), result.fechaCreacion());
        verify(repository, times(1)).save(any(Routine.class));
    }

    @Test
    void getRoutinesByUser_deberiaMapearTodasLasRutinasDelUsuario() {
        // Given
        Routine r1 = new Routine(1L, "Rutina A", "Fuerza", "coach.ana", LocalDate.now());
        Routine r2 = new Routine(1L, "Rutina B", "Cardio", "coach.ana", LocalDate.now());
        when(repository.findByUserId(1L)).thenReturn(List.of(r1, r2));

        // When
        List<RoutineResponse> result = routineService.getRoutinesByUser(1L);

        // Then
        assertEquals(2, result.size());
        assertEquals("Rutina A", result.get(0).nombre());
        assertEquals("Rutina B", result.get(1).nombre());
    }
}
