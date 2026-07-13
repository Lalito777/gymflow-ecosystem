package cl.joaedu.userservice.service;

import cl.joaedu.userservice.client.BranchClient;
import cl.joaedu.userservice.dto.UserRequest;
import cl.joaedu.userservice.dto.UserResponse;
import cl.joaedu.userservice.model.User;
import cl.joaedu.userservice.repository.UserRepository;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final Set<String> ROLES_VALIDOS = Set.of("SOCIO", "ADMIN");
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BranchClient branchClient;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, BranchClient branchClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.branchClient = branchClient;
    }

    public List<UserResponse> findAll() {
        log.debug("Buscando todos los usuarios del sistema");
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Regla de negocio: un socio se registra siempre asociado a una sucursal existente.
     * Se valida en vivo contra branch-service via Feign antes de guardar el usuario:
     * si la sucursal no existe, o branch-service no responde, no se crea el usuario.
     */
    public UserResponse create(UserRequest request) {
        if (!ROLES_VALIDOS.contains(request.role())) {
            throw new IllegalArgumentException("Rol invalido: " + request.role() + ". Valores permitidos: " + ROLES_VALIDOS);
        }

        verifyBranchExists(request.branchId());

        try {
            User user = new User(
                    request.name(),
                    request.email(),
                    request.subscriptionPlan(),
                    passwordEncoder.encode(request.password()),
                    request.role(),
                    request.branchId()
            );
            User savedUser = userRepository.save(user);
            log.info("Usuario creado: ID #{} - Email: {} - Sucursal #{}", savedUser.getId(), savedUser.getEmail(), savedUser.getBranchId());
            return mapToResponse(savedUser);
        } catch (Exception e) {
            log.error("Fallo al registrar usuario {}: {}", request.email(), e.getMessage());
            throw e;
        }
    }

    private void verifyBranchExists(Long branchId) {
        log.debug("Validando sucursal #{} contra branch-service (Feign)", branchId);
        try {
            branchClient.getBranchById(branchId);
        } catch (FeignException.NotFound e) {
            log.warn("Registro rechazado: la sucursal #{} no existe", branchId);
            throw new EntityNotFoundException("La sucursal " + branchId + " no existe");
        } catch (FeignException e) {
            log.error("branch-service no respondio al validar la sucursal #{}: {}", branchId, e.getMessage());
            throw new IllegalStateException("No fue posible validar la sucursal, intenta nuevamente");
        }
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getSubscriptionPlan(),
                user.getRole(),
                user.getBranchId()
        );
    }
}
