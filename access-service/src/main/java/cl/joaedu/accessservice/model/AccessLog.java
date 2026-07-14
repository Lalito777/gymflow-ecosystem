package cl.joaedu.accessservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs")
public class AccessLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long branchId;
    private String tipo;
    private LocalDateTime timestamp;

    public AccessLog() {}
    public AccessLog(Long userId, Long branchId, String tipo, LocalDateTime timestamp) {
        this.userId = userId;
        this.branchId = branchId;
        this.tipo = tipo;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getBranchId() { return branchId; }
    public String getTipo() { return tipo; }
    public LocalDateTime getTimestamp() { return timestamp; }
}