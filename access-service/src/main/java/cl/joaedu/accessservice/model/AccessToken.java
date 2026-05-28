package cl.joaedu.accessservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_tokens")
public class AccessToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long branchId;
    private String qrCode;
    private LocalDateTime fechaExpiracion;
    private String estado;

    public AccessToken() {}
    public AccessToken(Long userId, Long branchId, String qrCode, LocalDateTime fechaExpiracion, String estado) {
        this.userId = userId;
        this.branchId = branchId;
        this.qrCode = qrCode;
        this.fechaExpiracion = fechaExpiracion;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public String getQrCode() { return qrCode; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public Long getUserId() { return userId; }
}