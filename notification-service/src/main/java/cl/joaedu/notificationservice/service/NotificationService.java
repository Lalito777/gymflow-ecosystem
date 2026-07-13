package cl.joaedu.notificationservice.service;

import cl.joaedu.notificationservice.dto.NotificationRequest;
import cl.joaedu.notificationservice.dto.NotificationResponse;
import cl.joaedu.notificationservice.model.Notification;
import cl.joaedu.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Capa service que faltaba en notification-service: antes el controller
 * llamaba al repository directamente. Ahora la logica (incluida la validacion
 * del canal de notificacion) vive aca.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private static final Set<String> TIPOS_VALIDOS = Set.of("EMAIL", "SMS", "PUSH");

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Regla de negocio: el canal de notificacion debe ser uno de los soportados.
     */
    public NotificationResponse send(NotificationRequest request) {
        if (!TIPOS_VALIDOS.contains(request.tipo())) {
            throw new IllegalArgumentException("Tipo de notificacion invalido: " + request.tipo() +
                    ". Valores permitidos: " + TIPOS_VALIDOS);
        }

        Notification notification = new Notification(request.destinatarioId(), request.tipo(), request.mensaje(), LocalDateTime.now());
        Notification saved = repository.save(notification);
        log.info("Notificacion enviada: ID #{} - tipo {} - destinatario #{}", saved.getId(), saved.getTipo(), saved.getDestinatarioId());
        return mapToResponse(saved);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getDestinatarioId(),
                notification.getTipo(), notification.getMensaje(), notification.getCreadoEn());
    }
}
