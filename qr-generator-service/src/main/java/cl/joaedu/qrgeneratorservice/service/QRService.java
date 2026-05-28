package cl.joaedu.qrgeneratorservice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import cl.joaedu.qrgeneratorservice.model.QRData;
import cl.joaedu.qrgeneratorservice.repository.QRDataRepository;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class QRService {
    private final QRDataRepository repo;

    public QRService(QRDataRepository repo) {
        this.repo = repo;
    }

    public QRData generateQR(Long accessTokenId, String contenido) throws Exception {
        BitMatrix matrix = new MultiFormatWriter().encode(contenido, BarcodeFormat.QR_CODE, 300, 300);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        String base64 = Base64.getEncoder().encodeToString(out.toByteArray());

        QRData qr = new QRData(accessTokenId, contenido, "data:image/png;base64," + base64, LocalDateTime.now(), true);
        return repo.save(qr);
    }
}