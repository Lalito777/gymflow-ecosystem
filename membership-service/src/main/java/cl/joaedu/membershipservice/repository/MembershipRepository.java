package cl.joaedu.membershipservice.repository;

import cl.joaedu.membershipservice.model.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findTopByUserIdOrderByFechaVencimientoDesc(Long userId);
}