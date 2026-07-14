package cl.joaedu.equipmentservice.repository;

import cl.joaedu.equipmentservice.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByBranchId(Long branchId);
}