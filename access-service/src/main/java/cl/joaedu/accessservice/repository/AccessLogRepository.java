package cl.joaedu.accessservice.repository;

import cl.joaedu.accessservice.model.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
}