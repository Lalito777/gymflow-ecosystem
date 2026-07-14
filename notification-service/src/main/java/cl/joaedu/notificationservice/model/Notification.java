package cl.joaedu.notificationservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long destinatarioId;
    private String tipo;
    private String mensaje;
    private LocalDateTime creadoEn;

    public Notification() {}
    public Notification(Long destinatarioId, String tipo, String mensaje, LocalDateTime creadoEn) {
        this.destinatarioId = destinatarioId;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.creadoEn = creadoEn;
    }

    public Long getId() { return id; }
    public Long getDestinatarioId() { return destinatarioId; }
    public String getTipo() { return tipo; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}