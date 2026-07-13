package cl.joaedu.classservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_reservations")
public class ClassReservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long classId;
    private LocalDateTime fechaReserva;
    private String estado;

    public ClassReservation() {}
    public ClassReservation(Long userId, Long classId, LocalDateTime fechaReserva, String estado) {
        this.userId = userId;
        this.classId = classId;
        this.fechaReserva = fechaReserva;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getClassId() { return classId; }
    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}