package cl.joaedu.classservice.service;

import cl.joaedu.classservice.dto.ClassReservationResponse;
import cl.joaedu.classservice.dto.MembershipStatusDto;
import cl.joaedu.classservice.dto.ReserveClassRequest;
import cl.joaedu.classservice.model.ClassReservation;
import cl.joaedu.classservice.repository.ClassReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassServiceTest {

    @Mock
    private ClassReservationRepository repository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient membershipRestClient;

    private ClassService classService;

    private void withMembershipStatus(MembershipStatusDto dto) {
        when(membershipRestClient.get()
                .uri(anyString(), eq(1L))
                .retrieve()
                .body(MembershipStatusDto.class))
                .thenReturn(dto);
        classService = new ClassService(repository, membershipRestClient);
    }

    @Test
    void reserve_conMembresiaActivaYSinReservaPrevia_deberiaCrearlaConfirmada() {
        // Given
        withMembershipStatus(new MembershipStatusDto(1L, true));
        ReserveClassRequest request = new ReserveClassRequest(1L, 10L);
        when(repository.findByUserIdAndClassIdAndEstado(1L, 10L, "CONFIRMADA")).thenReturn(Optional.empty());
        when(repository.save(any(ClassReservation.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        ClassReservationResponse result = classService.reserve(request);

        // Then
        assertEquals("CONFIRMADA", result.estado());
        verify(repository, times(1)).save(any(ClassReservation.class));
    }

    @Test
    void reserve_conMembresiaInactiva_deberiaLanzarExcepcionYNoGuardar() {
        // Given
        withMembershipStatus(new MembershipStatusDto(1L, false));
        ReserveClassRequest request = new ReserveClassRequest(1L, 10L);

        // When / Then
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> classService.reserve(request));
        assertEquals("El usuario no tiene una membresia activa", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void reserve_siMembershipServiceNoResponde_deberiaLanzarExcepcion() {
        // Given: membership-service cae o hace timeout
        when(membershipRestClient.get()
                .uri(anyString(), eq(1L))
                .retrieve()
                .body(MembershipStatusDto.class))
                .thenThrow(new RestClientException("timeout"));
        classService = new ClassService(repository, membershipRestClient);
        ReserveClassRequest request = new ReserveClassRequest(1L, 10L);

        // When / Then
        assertThrows(IllegalStateException.class, () -> classService.reserve(request));
        verify(repository, never()).save(any());
    }

    @Test
    void reserve_conReservaYaConfirmadaParaLaMismaClase_deberiaLanzarExcepcion() {
        // Given
        withMembershipStatus(new MembershipStatusDto(1L, true));
        ReserveClassRequest request = new ReserveClassRequest(1L, 10L);
        ClassReservation existente = new ClassReservation(1L, 10L, LocalDateTime.now(), "CONFIRMADA");
        when(repository.findByUserIdAndClassIdAndEstado(1L, 10L, "CONFIRMADA")).thenReturn(Optional.of(existente));

        // When / Then
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> classService.reserve(request));
        assertEquals("El usuario ya tiene una reserva confirmada para esta clase", ex.getMessage());
        verify(repository, never()).save(any());
    }
}
