package cl.joaedu.classservice.service;

import cl.joaedu.classservice.model.ClassReservation;
import cl.joaedu.classservice.repository.ClassReservationRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ClassService {
    private final ClassReservationRepository repository;

    public ClassService(ClassReservationRepository repository) {
        this.repository = repository;
    }

    public ClassReservation reserve(Long userId, Long classId) {
        ClassReservation res = new ClassReservation(userId, classId, LocalDateTime.now(), "CONFIRMADA");
        return repository.save(res);
    }
}