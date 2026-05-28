package cl.joaedu.userservice.service;

import cl.joaedu.userservice.dto.UserRequest;
import cl.joaedu.userservice.dto.UserResponse;
import cl.joaedu.userservice.model.User;
import cl.joaedu.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> findAll() {
        log.debug("Buscando todos los usuarios del sistema");
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse create(UserRequest request) {
        try {
            User user = new User(
                    request.name(),
                    request.email(),
                    request.subscriptionPlan(),
                    passwordEncoder.encode(request.password()),
                    request.role()
            );
            User savedUser = userRepository.save(user);
            log.info("Usuario creado: ID #{} - Email: {}", savedUser.getId(), savedUser.getEmail());
            return mapToResponse(savedUser);
        } catch (Exception e) {
            log.error("Fallo al registrar usuario {}: {}", request.email(), e.getMessage());
            throw e;
        }
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getSubscriptionPlan(),
                user.getRole()
        );
    }
}