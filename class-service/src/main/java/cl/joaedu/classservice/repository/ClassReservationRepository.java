package cl.joaedu.classservice.repository;

import cl.joaedu.classservice.model.ClassReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassReservationRepository extends JpaRepository<ClassReservation, Long> {
}