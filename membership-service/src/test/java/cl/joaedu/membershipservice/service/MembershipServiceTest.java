package cl.joaedu.membershipservice.service;

import cl.joaedu.membershipservice.dto.MembershipRequest;
import cl.joaedu.membershipservice.dto.MembershipResponse;
import cl.joaedu.membershipservice.model.Membership;
import cl.joaedu.membershipservice.model.Plan;
import cl.joaedu.membershipservice.repository.MembershipRepository;
import cl.joaedu.membershipservice.repository.PlanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private MembershipRepository membershipRepo;
    @Mock
    private PlanRepository planRepo;

    @InjectMocks
    private MembershipService membershipService;

    @Test
    void isMembershipActive_conMembresiaVigenteYActiva_deberiaRetornarTrue() {
        // Given
        Membership m = new Membership(1L, 1L, LocalDate.now().minusDays(5), LocalDate.now().plusDays(25), "ACTIVA");
        when(membershipRepo.findTopByUserIdOrderByFechaVencimientoDesc(1L)).thenReturn(Optional.of(m));

        // When
        boolean result = membershipService.isMembershipActive(1L);

        // Then
        assertTrue(result);
    }

    @Test
    void isMembershipActive_conMembresiaVencida_deberiaRetornarFalse() {
        // Given
        Membership m = new Membership(1L, 1L, LocalDate.now().minusDays(60), LocalDate.now().minusDays(5), "ACTIVA");
        when(membershipRepo.findTopByUserIdOrderByFechaVencimientoDesc(1L)).thenReturn(Optional.of(m));

        // When
        boolean result = membershipService.isMembershipActive(1L);

        // Then
        assertFalse(result);
    }

    @Test
    void isMembershipActive_sinMembresiaRegistrada_deberiaRetornarFalse() {
        // Given
        when(membershipRepo.findTopByUserIdOrderByFechaVencimientoDesc(1L)).thenReturn(Optional.empty());

        // When
        boolean result = membershipService.isMembershipActive(1L);

        // Then
        assertFalse(result);
    }

    @Test
    void getStatus_deberiaEnvolverElResultadoDeIsMembershipActiveEnElDto() {
        // Given
        when(membershipRepo.findTopByUserIdOrderByFechaVencimientoDesc(1L)).thenReturn(Optional.empty());

        // When
        var status = membershipService.getStatus(1L);

        // Then
        assertEquals(1L, status.userId());
        assertFalse(status.activa());
    }

    @Test
    void createMembership_conPlanExistente_deberiaCalcularFechaVencimientoYGuardar() {
        // Given
        Plan plan = new Plan("Mensual", new BigDecimal("19990"), 30, "Plan mensual basico");
        MembershipRequest request = new MembershipRequest(1L, 1L);
        when(planRepo.findById(1L)).thenReturn(Optional.of(plan));
        when(membershipRepo.save(any(Membership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MembershipResponse result = membershipService.createMembership(request);

        // Then
        assertEquals("ACTIVA", result.estado());
        assertEquals(LocalDate.now().plusDays(30), result.fechaVencimiento());
        verify(membershipRepo, times(1)).save(any(Membership.class));
    }

    @Test
    void createMembership_conPlanInexistente_deberiaLanzarEntityNotFoundException() {
        // Given
        MembershipRequest request = new MembershipRequest(1L, 99L);
        when(planRepo.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> membershipService.createMembership(request));
        assertTrue(ex.getMessage().contains("99"));
        verify(membershipRepo, never()).save(any());
    }
}
