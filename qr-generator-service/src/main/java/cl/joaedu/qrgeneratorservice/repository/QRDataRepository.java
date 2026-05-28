package cl.joaedu.qrgeneratorservice.repository;

import cl.joaedu.qrgeneratorservice.model.QRData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QRDataRepository extends JpaRepository<QRData, Long> {
}