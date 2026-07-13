package cl.joaedu.qrgeneratorservice.service;

import cl.joaedu.qrgeneratorservice.dto.QrResponse;
import cl.joaedu.qrgeneratorservice.model.QRData;
import cl.joaedu.qrgeneratorservice.repository.QRDataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QRServiceTest {

    @Mock
    private QRDataRepository repo;

    @InjectMocks
    private QRService qrService;

    @Test
    void generateQR_conContenidoValido_deberiaGenerarImagenBase64YGuardar() throws Exception {
        // Given
        when(repo.save(any(QRData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        QrResponse result = qrService.generateQR(1L, "TOKEN-ABC-123");

        // Then
        assertNotNull(result);
        assertEquals(1L, result.accessTokenId());
        assertEquals("TOKEN-ABC-123", result.contenidoQr());
        assertTrue(result.imagenBase64().startsWith("data:image/png;base64,"));
        verify(repo, times(1)).save(any(QRData.class));
    }

    @Test
    void generateQR_conOtroAccessTokenId_deberiaPreservarElIdRecibido() throws Exception {
        // Given
        when(repo.save(any(QRData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        QrResponse result = qrService.generateQR(42L, "TOKEN-XYZ-999");

        // Then
        assertEquals(42L, result.accessTokenId());
        assertEquals("TOKEN-XYZ-999", result.contenidoQr());
        verify(repo, times(1)).save(any(QRData.class));
    }
}
