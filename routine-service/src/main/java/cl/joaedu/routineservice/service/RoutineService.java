package cl.joaedu.routineservice.service;

import cl.joaedu.routineservice.model.Routine;
import cl.joaedu.routineservice.repository.RoutineRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class RoutineService {
    private final RoutineRepository repository;

    public RoutineService(RoutineRepository repository) {
        this.repository = repository;
    }

    public List<Routine> getRoutinesByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    public Routine createRoutine(Routine routine) {
        Routine r = new Routine(routine.getUserId(), routine.getNombre(), routine.getObjetivo(), routine.getCreadoPor(), LocalDate.now());
        return repository.save(r);
    }
}