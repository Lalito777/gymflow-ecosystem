package cl.joaedu.classservice.repository;

import cl.joaedu.classservice.model.ClassReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassReservationRepository extends JpaRepository<ClassReservation, Long> {
    Optional<ClassReservation> findByUserIdAndClassIdAndEstado(Long userId, Long classId, String estado);
}
