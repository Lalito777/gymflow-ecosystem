package cl.joaedu.accessservice.service;

import cl.joaedu.accessservice.client.CapacityClient;
import cl.joaedu.accessservice.client.MembershipClient;
import cl.joaedu.accessservice.model.AccessLog;
import cl.joaedu.accessservice.model.AccessToken;
import cl.joaedu.accessservice.repository.AccessLogRepository;
import cl.joaedu.accessservice.repository.AccessTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {

    @Mock
    private AccessTokenRepository tokenRepo;
    @Mock
    private AccessLogRepository logRepo;
    @Mock
    private MembershipClient membershipClient;
    @Mock
    private CapacityClient capacityClient;

    @InjectMocks
    private AccessService accessService;

    private AccessToken token;

    @BeforeEach
    void setUp() {
        token = new AccessToken(1L, 1L, "QR-1234", LocalDateTime.now().plusHours(1), "PENDIENTE");
    }

    @Test
    void generateToken_conMembresiaActiva_deberiaCrearToken() {
        // Given
        when(membershipClient.getStatus(1L)).thenReturn(Map.of("activa", true));
        when(tokenRepo.save(any(AccessToken.class))).thenReturn(token);

        // When
        AccessToken result = accessService.generateToken(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals("PENDIENTE", result.getEstado());
        verify(tokenRepo, times(1)).save(any(AccessToken.class));
    }

    @Test
    void generateToken_conMembresiaInactiva_deberiaLanzarExcepcion() {
        // Given
        when(membershipClient.getStatus(1L)).thenReturn(Map.of("activa", false));

        // When / Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accessService.generateToken(1L, 1L));
        assertEquals("Membresia no activa en el sistema", ex.getMessage());
        verify(tokenRepo, never()).save(any());
    }

    @Test
    void validateEntry_conQrValido_deberiaRegistrarEntradaYActualizarToken() {
        // Given
        when(tokenRepo.findByQrCode("QR-1234")).thenReturn(Optional.of(token));
        AccessLog log = new AccessLog(1L, 1L, "ENTRADA", LocalDateTime.now());
        when(logRepo.save(any(AccessLog.class))).thenReturn(log);

        // When
        AccessLog result = accessService.validateEntry("QR-1234", 1L);

        // Then
        assertNotNull(result);
        assertEquals("USADO", token.getEstado());
        verify(capacityClient, times(1)).increment(1L);
        verify(logRepo, times(1)).save(any(AccessLog.class));
    }

    @Test
    void validateEntry_conQrInexistente_deberiaLanzarExcepcion() {
        // Given
        when(tokenRepo.findByQrCode("QR-NOEXISTE")).thenReturn(Optional.empty());

        // When / Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accessService.validateEntry("QR-NOEXISTE", 1L));
        assertEquals("QR Invalido", ex.getMessage());
    }

    @Test
    void validateEntry_conQrYaUsado_deberiaLanzarExcepcion() {
        // Given
        token.setEstado("USADO");
        when(tokenRepo.findByQrCode("QR-1234")).thenReturn(Optional.of(token));

        // When / Then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accessService.validateEntry("QR-1234", 1L));
        assertEquals("QR Expirado o ya utilizado", ex.getMessage());
    }

    @Test
    void validateEntry_conTokenExpirado_deberiaLanzarExcepcion() {
        // Given
        AccessToken expirado = new AccessToken(1L, 1L, "QR-9999", LocalDateTime.now().minusMinutes(1), "PENDIENTE");
        when(tokenRepo.findByQrCode("QR-9999")).thenReturn(Optional.of(expirado));

        // When / Then
        assertThrows(RuntimeException.class, () -> accessService.validateEntry("QR-9999", 1L));
    }

    @Test
    void validateEntry_siCapacityServiceFalla_deberiaContinuarSinLanzarExcepcion() {
        // Given
        when(tokenRepo.findByQrCode("QR-1234")).thenReturn(Optional.of(token));
        doThrow(new RuntimeException("Servicio no disponible")).when(capacityClient).increment(1L);
        when(logRepo.save(any(AccessLog.class))).thenReturn(new AccessLog(1L, 1L, "ENTRADA", LocalDateTime.now()));

        // When
        AccessLog result = accessService.validateEntry("QR-1234", 1L);

        // Then
        assertNotNull(result);
        verify(logRepo, times(1)).save(any(AccessLog.class));
    }
}
