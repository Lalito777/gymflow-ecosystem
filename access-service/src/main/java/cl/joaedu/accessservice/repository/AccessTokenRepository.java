package cl.joaedu.accessservice.repository;

import cl.joaedu.accessservice.model.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    Optional<AccessToken> findByQrCode(String qrCode);
}