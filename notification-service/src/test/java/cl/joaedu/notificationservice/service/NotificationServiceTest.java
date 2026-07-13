package cl.joaedu.notificationservice.service;

import cl.joaedu.notificationservice.dto.NotificationRequest;
import cl.joaedu.notificationservice.dto.NotificationResponse;
import cl.joaedu.notificationservice.model.Notification;
import cl.joaedu.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void send_conTipoValido_deberiaGuardarLaNotificacion() {
        // Given
        NotificationRequest request = new NotificationRequest(1L, "EMAIL", "Tu membresia vence en 3 dias");
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        NotificationResponse result = notificationService.send(request);

        // Then
        assertEquals("EMAIL", result.tipo());
        assertEquals(1L, result.destinatarioId());
        assertNotNull(result.creadoEn());
        verify(repository, times(1)).save(any(Notification.class));
    }

    @Test
    void send_conTipoInvalido_deberiaLanzarExcepcion() {
        // Given: regla de negocio -> el canal debe ser EMAIL, SMS o PUSH
        NotificationRequest request = new NotificationRequest(1L, "FAX", "mensaje");

        // When / Then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> notificationService.send(request));
        assertTrue(ex.getMessage().contains("Tipo de notificacion invalido"));
        verify(repository, never()).save(any());
    }
}
