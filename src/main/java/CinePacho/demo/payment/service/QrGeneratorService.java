package CinePacho.demo.payment.service;

import CinePacho.demo.exception.CinePachoException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

// shared/QrGeneratorService.java
@Service
public class QrGeneratorService {

    /**
     * Genera un QR a partir de un texto y lo devuelve como String base64.
     * El texto será la URL que el empleado escaneará para validar la entrada.
     * @param content El texto que codifica el QR (ej: URL de validación)
     * @param width Ancho del QR en píxeles
     * @param height Alto del QR en píxeles
     * @return String base64 del QR listo para embeber en HTML o enviar por correo
     */
    public String generateQrBase64(String content, int width, int height) {
        try {
            // Configuración del QR: corrección de errores alta para que resista impresión
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);

            // Genera la matriz de bits del QR
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints
            );

            // Convierte la matriz a imagen PNG en memoria
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            // Convierte los bytes de la imagen a base64
            byte[] qrBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(qrBytes);

        } catch (Exception e) {
            throw new CinePachoException("Error generando el QR: " + e.getMessage());
        }
    }
}
