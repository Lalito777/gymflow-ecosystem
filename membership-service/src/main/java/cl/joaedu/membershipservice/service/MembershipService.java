package cl.joaedu.membershipservice.service;

import cl.joaedu.membershipservice.dto.MembershipRequest;
import cl.joaedu.membershipservice.dto.MembershipResponse;
import cl.joaedu.membershipservice.dto.MembershipStatusResponse;
import cl.joaedu.membershipservice.model.Membership;
import cl.joaedu.membershipservice.model.Plan;
import cl.joaedu.membershipservice.repository.MembershipRepository;
import cl.joaedu.membershipservice.repository.PlanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class MembershipService {

    private static final Logger log = LoggerFactory.getLogger(MembershipService.class);

    private final MembershipRepository membershipRepo;
    private final PlanRepository planRepo;

    public MembershipService(MembershipRepository membershipRepo, PlanRepository planRepo) {
        this.membershipRepo = membershipRepo;
        this.planRepo = planRepo;
    }

    /**
     * Regla de negocio: una membresia esta activa solo si su estado es ACTIVA
     * Y la fecha de vencimiento todavia no paso. Este metodo es consumido en vivo
     * por access-service via Feign antes de emitir un token de acceso.
     */
    public boolean isMembershipActive(Long userId) {
        return membershipRepo.findTopByUserIdOrderByFechaVencimientoDesc(userId)
                .map(m -> m.getEstado().equals("ACTIVA") && m.getFechaVencimiento().isAfter(LocalDate.now()))
                .orElse(false);
    }

    public MembershipStatusResponse getStatus(Long userId) {
        return new MembershipStatusResponse(userId, isMembershipActive(userId));
    }

    public MembershipResponse createMembership(MembershipRequest request) {
        Plan plan = planRepo.findById(request.planId())
                .orElseThrow(() -> new EntityNotFoundException("Plan no existente: " + request.planId()));

        Membership membership = new Membership(
                request.userId(),
                request.planId(),
                LocalDate.now(),
                LocalDate.now().plusDays(plan.getDuracionDias()),
                "ACTIVA"
        );
        Membership saved = membershipRepo.save(membership);
        log.info("Membresia creada: ID #{} - usuario #{} - plan #{} - vence {}",
                saved.getId(), saved.getUserId(), saved.getPlanId(), saved.getFechaVencimiento());
        return mapToResponse(saved);
    }

    private MembershipResponse mapToResponse(Membership membership) {
        return new MembershipResponse(
                membership.getId(),
                membership.getUserId(),
                membership.getPlanId(),
                membership.getFechaInicio(),
                membership.getFechaVencimiento(),
                membership.getEstado()
        );
    }
}
