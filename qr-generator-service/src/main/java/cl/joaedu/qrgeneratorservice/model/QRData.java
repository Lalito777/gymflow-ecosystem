package cl.joaedu.qrgeneratorservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "qr_data")
public class QRData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long accessTokenId;
    private String contenidoQr;
    @Lob
    private String imagenBase64;
    private LocalDateTime fechaCreacion;
    private Boolean vigente;

    public QRData() {}
    public QRData(Long accessTokenId, String contenidoQr, String imagenBase64, LocalDateTime fechaCreacion, Boolean vigente) {
        this.accessTokenId = accessTokenId;
        this.contenidoQr = contenidoQr;
        this.imagenBase64 = imagenBase64;
        this.fechaCreacion = fechaCreacion;
        this.vigente = vigente;
    }

    public Long getId() { return id; }
    public String getImagenBase64() { return imagenBase64; }
    public Long getAccessTokenId() { return accessTokenId; }
    public String getContenidoQr() { return contenidoQr; }
}