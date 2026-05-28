package cl.joaedu.membershipservice.service;

import cl.joaedu.membershipservice.model.Membership;
import cl.joaedu.membershipservice.model.Plan;
import cl.joaedu.membershipservice.repository.MembershipRepository;
import cl.joaedu.membershipservice.repository.PlanRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class MembershipService {
    private final MembershipRepository membershipRepo;
    private final PlanRepository planRepo;

    public MembershipService(MembershipRepository membershipRepo, PlanRepository planRepo) {
        this.membershipRepo = membershipRepo;
        this.planRepo = planRepo;
    }

    public boolean isMembershipActive(Long userId) {
        return membershipRepo.findTopByUserIdOrderByFechaVencimientoDesc(userId)
                .map(m -> m.getEstado().equals("ACTIVA") && m.getFechaVencimiento().isAfter(LocalDate.now()))
                .orElse(false);
    }

    public Membership createMembership(Long userId, Long planId) {
        Plan plan = planRepo.findById(planId).orElseThrow(() -> new RuntimeException("Plan no existente"));
        Membership m = new Membership(userId, planId, LocalDate.now(), LocalDate.now().plusDays(plan.getDuracionDias()), "ACTIVA");
        return membershipRepo.save(m);
    }
}