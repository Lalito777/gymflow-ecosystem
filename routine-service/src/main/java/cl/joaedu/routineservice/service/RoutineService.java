package cl.joaedu.routineservice.service;

import cl.joaedu.routineservice.dto.RoutineRequest;
import cl.joaedu.routineservice.dto.RoutineResponse;
import cl.joaedu.routineservice.model.Routine;
import cl.joaedu.routineservice.repository.RoutineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoutineService {

    private static final Logger log = LoggerFactory.getLogger(RoutineService.class);

    private final RoutineRepository repository;

    public RoutineService(RoutineRepository repository) {
        this.repository = repository;
    }

    public List<RoutineResponse> getRoutinesByUser(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public RoutineResponse createRoutine(RoutineRequest request) {
        Routine routine = new Routine(request.userId(), request.nombre(), request.objetivo(), request.creadoPor(), LocalDate.now());
        Routine saved = repository.save(routine);
        log.info("Rutina creada: ID #{} - usuario #{} - nombre {}", saved.getId(), saved.getUserId(), saved.getNombre());
        return mapToResponse(saved);
    }

    private RoutineResponse mapToResponse(Routine routine) {
        return new RoutineResponse(routine.getId(), routine.getUserId(), routine.getNombre(),
                routine.getObjetivo(), routine.getCreadoPor(), routine.getFechaCreacion());
    }
}
