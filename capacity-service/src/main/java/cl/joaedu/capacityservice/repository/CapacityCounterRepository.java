package cl.joaedu.capacityservice.repository;

import cl.joaedu.capacityservice.model.CapacityCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CapacityCounterRepository extends JpaRepository<CapacityCounter, Long> {
    Optional<CapacityCounter> findByBranchId(Long branchId);
}