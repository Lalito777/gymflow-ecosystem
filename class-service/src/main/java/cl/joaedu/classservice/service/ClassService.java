package cl.joaedu.classservice.service;

import cl.joaedu.classservice.dto.ClassReservationResponse;
import cl.joaedu.classservice.dto.MembershipStatusDto;
import cl.joaedu.classservice.dto.ReserveClassRequest;
import cl.joaedu.classservice.model.ClassReservation;
import cl.joaedu.classservice.repository.ClassReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;

@Service
public class ClassService {

    private static final Logger log = LoggerFactory.getLogger(ClassService.class);

    private final ClassReservationRepository repository;
    private final RestClient membershipRestClient;

    public ClassService(ClassReservationRepository repository, RestClient membershipRestClient) {
        this.repository = repository;
        this.membershipRestClient = membershipRestClient;
    }

    /**
     * Reglas de negocio:
     * 1. El socio debe tener una membresia activa (se valida en vivo contra membership-service via RestClient).
     * 2. No puede tener dos reservas CONFIRMADA para la misma clase.
     */
    public ClassReservationResponse reserve(ReserveClassRequest request) {
        verifyActiveMembership(request.userId());

        repository.findByUserIdAndClassIdAndEstado(request.userId(), request.classId(), "CONFIRMADA")
                .ifPresent(r -> {
                    throw new IllegalStateException("El usuario ya tiene una reserva confirmada para esta clase");
                });

        ClassReservation reservation = new ClassReservation(request.userId(), request.classId(), LocalDateTime.now(), "CONFIRMADA");
        ClassReservation saved = repository.save(reservation);
        log.info("Reserva creada: ID #{} - usuario #{} - clase #{}", saved.getId(), saved.getUserId(), saved.getClassId());
        return mapToResponse(saved);
    }

    private void verifyActiveMembership(Long userId) {
        log.debug("Consultando estado de membresia (RestClient) para usuario #{}", userId);
        MembershipStatusDto status;
        try {
            status = membershipRestClient.get()
                    .uri("/api/membership/status/{userId}", userId)
                    .retrieve()
                    .body(MembershipStatusDto.class);
        } catch (RestClientException e) {
            log.error("membership-service no respondio al validar reserva de usuario #{}: {}", userId, e.getMessage());
            throw new IllegalStateException("No fue posible verificar la membresia, intenta nuevamente");
        }

        if (status == null || !status.activa()) {
            log.warn("Reserva denegada: membresia no activa para usuario #{}", userId);
            throw new IllegalStateException("El usuario no tiene una membresia activa");
        }
    }

    private ClassReservationResponse mapToResponse(ClassReservation reservation) {
        return new ClassReservationResponse(reservation.getId(), reservation.getUserId(), reservation.getClassId(),
                reservation.getFechaReserva(), reservation.getEstado());
    }
}
