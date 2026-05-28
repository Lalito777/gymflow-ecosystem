package cl.joaedu.membershipservice.repository;

import cl.joaedu.membershipservice.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}