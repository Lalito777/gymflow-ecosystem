package cl.joaedu.userservice.service;

import cl.joaedu.userservice.dto.UserRequest;
import cl.joaedu.userservice.dto.UserResponse;
import cl.joaedu.userservice.model.User;
import cl.joaedu.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void create_conDatosValidos_deberiaEncriptarPasswordYGuardarUsuario() {
        // Given
        UserRequest request = new UserRequest("Eduardo", "eduardo@mail.com", "PREMIUM", "clave123", "SOCIO");
        when(passwordEncoder.encode("clave123")).thenReturn("HASH_CLAVE123");
        User saved = new User("Eduardo", "eduardo@mail.com", "PREMIUM", "HASH_CLAVE123", "SOCIO");
        saved.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        // When
        UserResponse result = userService.create(request);

        // Then
        assertEquals("eduardo@mail.com", result.email());
        verify(passwordEncoder, times(1)).encode("clave123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void create_siRepositorioFalla_deberiaPropagarExcepcion() {
        // Given
        UserRequest request = new UserRequest("Eduardo", "eduardo@mail.com", "PREMIUM", "clave123", "SOCIO");
        when(passwordEncoder.encode("clave123")).thenReturn("HASH_CLAVE123");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Email duplicado"));

        // When / Then
        assertThrows(RuntimeException.class, () -> userService.create(request));
    }

    @Test
    void findAll_conUsuariosRegistrados_deberiaRetornarListaMapeada() {
        // Given
        User u1 = new User("Eduardo", "eduardo@mail.com", "PREMIUM", "HASH", "SOCIO");
        u1.setId(1L);
        User u2 = new User("Ana", "ana@mail.com", "BASICO", "HASH2", "SOCIO");
        u2.setId(2L);
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        // When
        List<UserResponse> result = userService.findAll();

        // Then
        assertEquals(2, result.size());
        assertEquals("eduardo@mail.com", result.get(0).email());
    }

    @Test
    void findAll_sinUsuarios_deberiaRetornarListaVacia() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<UserResponse> result = userService.findAll();

        // Then
        assertTrue(result.isEmpty());
    }
}
