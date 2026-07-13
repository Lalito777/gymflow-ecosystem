package cl.joaedu.accessservice.service;

import cl.joaedu.accessservice.client.CapacityClient;
import cl.joaedu.accessservice.client.MembershipClient;
import cl.joaedu.accessservice.dto.AccessLogResponse;
import cl.joaedu.accessservice.dto.AccessTokenResponse;
import cl.joaedu.accessservice.dto.MembershipStatusDto;
import cl.joaedu.accessservice.model.AccessLog;
import cl.joaedu.accessservice.model.AccessToken;
import cl.joaedu.accessservice.repository.AccessLogRepository;
import cl.joaedu.accessservice.repository.AccessTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AccessService {

    private static final Logger log = LoggerFactory.getLogger(AccessService.class);

    private final AccessTokenRepository tokenRepo;
    private final AccessLogRepository logRepo;
    private final MembershipClient membershipClient;
    private final CapacityClient capacityClient;

    public AccessService(AccessTokenRepository tokenRepo, AccessLogRepository logRepo,
                          MembershipClient membershipClient, CapacityClient capacityClient) {
        this.tokenRepo = tokenRepo;
        this.logRepo = logRepo;
        this.membershipClient = membershipClient;
        this.capacityClient = capacityClient;
    }

    /**
     * Regla de negocio: no se emite token de acceso si la membresia del socio no esta activa.
     * Se consulta a membership-service en vivo via Feign antes de crear el token.
     */
    public AccessTokenResponse generateToken(Long userId, Long branchId) {
        log.debug("Consultando estado de membresia remoto para usuario #{}", userId);
        MembershipStatusDto status = membershipClient.getStatus(userId);

        if (status == null || !status.activa()) {
            log.warn("Token denegado: membresia no activa para usuario #{}", userId);
            throw new IllegalStateException("Membresia no activa en el sistema");
        }

        AccessToken token = new AccessToken(userId, branchId, UUID.randomUUID().toString(),
                LocalDateTime.now().plusHours(24), "PENDIENTE");
        AccessToken saved = tokenRepo.save(token);
        log.info("Token de acceso generado: ID #{} - usuario #{} - sucursal #{}", saved.getId(), userId, branchId);
        return mapToResponse(saved);
    }

    /**
     * Regla de negocio: el QR debe existir, estar PENDIENTE y no haber expirado.
     * Al validar la entrada se marca como USADO (evita reuso) y se notifica a capacity-service
     * de forma tolerante a fallas (si capacity-service no responde, el ingreso igual se registra).
     */
    public AccessLogResponse validateEntry(String qrCode, Long branchId) {
        AccessToken token = tokenRepo.findByQrCode(qrCode)
                .orElseThrow(() -> new EntityNotFoundException("QR invalido: " + qrCode));

        if (!token.getEstado().equals("PENDIENTE") || token.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            log.warn("QR rechazado (expirado o ya usado): {}", qrCode);
            throw new IllegalStateException("QR expirado o ya utilizado");
        }

        token.setEstado("USADO");
        tokenRepo.save(token);

        try {
            capacityClient.increment(branchId);
        } catch (Exception e) {
            log.warn("capacity-service no disponible al incrementar sucursal #{}: {}", branchId, e.getMessage());
        }

        AccessLog accessLog = new AccessLog(token.getUserId(), branchId, "ENTRADA", LocalDateTime.now());
        AccessLog saved = logRepo.save(accessLog);
        log.info("Entrada registrada: usuario #{} - sucursal #{}", saved.getUserId(), saved.getBranchId());
        return mapToLogResponse(saved);
    }

    private AccessTokenResponse mapToResponse(AccessToken token) {
        return new AccessTokenResponse(token.getId(), token.getUserId(), token.getQrCode(),
                token.getFechaExpiracion(), token.getEstado());
    }

    private AccessLogResponse mapToLogResponse(AccessLog accessLog) {
        return new AccessLogResponse(accessLog.getId(), accessLog.getUserId(), accessLog.getBranchId(),
                accessLog.getTipo(), accessLog.getTimestamp());
    }
}
