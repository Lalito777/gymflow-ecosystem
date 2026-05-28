package cl.joaedu.accessservice.service;

import cl.joaedu.accessservice.client.CapacityClient;
import cl.joaedu.accessservice.client.MembershipClient;
import cl.joaedu.accessservice.model.AccessLog;
import cl.joaedu.accessservice.model.AccessToken;
import cl.joaedu.accessservice.repository.AccessLogRepository;
import cl.joaedu.accessservice.repository.AccessTokenRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AccessService {
    private final AccessTokenRepository tokenRepo;
    private final AccessLogRepository logRepo;
    private final MembershipClient membershipClient;
    private final CapacityClient capacityClient;

    public AccessService(AccessTokenRepository tokenRepo, AccessLogRepository logRepo, MembershipClient membershipClient, CapacityClient capacityClient) {
        this.tokenRepo = tokenRepo;
        this.logRepo = logRepo;
        this.membershipClient = membershipClient;
        this.capacityClient = capacityClient;
    }

    public AccessToken generateToken(Long userId, Long branchId) {
        Map<String, Boolean> status = membershipClient.getStatus(userId);
        if (!Boolean.TRUE.equals(status.get("activa"))) {
            throw new RuntimeException("Membresia no activa en el sistema");
        }
        AccessToken token = new AccessToken(userId, branchId, UUID.randomUUID().toString(), LocalDateTime.now().plusHours(24), "PENDIENTE");
        return tokenRepo.save(token);
    }

    public AccessLog validateEntry(String qrCode, Long branchId) {
        AccessToken token = tokenRepo.findByQrCode(qrCode).orElseThrow(() -> new RuntimeException("QR Invalido"));
        if (!token.getEstado().equals("PENDIENTE") || token.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("QR Expirado o ya utilizado");
        }
        token.setEstado("USADO");
        tokenRepo.save(token);

        try {
            capacityClient.increment(branchId);
        } catch(Exception e) {
            // Falla tolerante si el servicio de capacidad aun no esta levantado
        }

        AccessLog log = new AccessLog(token.getUserId(), branchId, "ENTRADA", LocalDateTime.now());
        return logRepo.save(log);
    }
}