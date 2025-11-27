package com.allergypassport.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

/**
 * Service for generating QR codes using ZXing library.
 */
@Service
public class QRCodeService {

    private static final Logger log = LoggerFactory.getLogger(QRCodeService.class);

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.qr.width:300}")
    private int defaultWidth;

    @Value("${app.qr.height:300}")
    private int defaultHeight;

    // QR code colors
    private static final int QR_ON_COLOR = 0xFF1F2937;  // Dark gray (Tailwind gray-800)
    private static final int QR_OFF_COLOR = 0xFFFFFFFF;  // White

    /**
     * Generate a QR code image as PNG bytes for the given user's public URL.
     *
     * @param publicId The user's public ID
     * @return PNG image as byte array
     */
    public byte[] generateQRCode(String publicId) throws WriterException, IOException {
        return generateQRCode(publicId, defaultWidth, defaultHeight);
    }

    /**
     * Generate a QR code image as PNG bytes with custom dimensions.
     *
     * @param publicId The user's public ID
     * @param width    QR code width in pixels
     * @param height   QR code height in pixels
     * @return PNG image as byte array
     */
    public byte[] generateQRCode(String publicId, int width, int height) throws WriterException, IOException {
        String url = buildPublicUrl(publicId);
        return generateQRCodeForUrl(url, width, height);
    }

    /**
     * Generate a QR code image for any URL.
     *
     * @param url    The URL to encode
     * @param width  QR code width in pixels
     * @param height QR code height in pixels
     * @return PNG image as byte array
     */
    public byte[] generateQRCodeForUrl(String url, int width, int height) throws WriterException, IOException {
        log.debug("Generating QR code for URL: {} ({}x{})", url, width, height);

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);  // High error correction
        hints.put(EncodeHintType.MARGIN, 2);  // Small margin
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, width, height, hints);

        // Create image with custom colors
        MatrixToImageConfig config = new MatrixToImageConfig(QR_ON_COLOR, QR_OFF_COLOR);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream, config);

        return outputStream.toByteArray();
    }

    /**
     * Generate a QR code as a Base64-encoded data URL (for inline embedding in HTML).
     *
     * @param publicId The user's public ID
     * @return Data URL string (data:image/png;base64,...)
     */
    public String generateQRCodeAsDataUrl(String publicId) throws WriterException, IOException {
        return generateQRCodeAsDataUrl(publicId, defaultWidth, defaultHeight);
    }

    /**
     * Generate a QR code as a Base64-encoded data URL with custom dimensions.
     *
     * @param publicId The user's public ID
     * @param width    QR code width in pixels
     * @param height   QR code height in pixels
     * @return Data URL string (data:image/png;base64,...)
     */
    public String generateQRCodeAsDataUrl(String publicId, int width, int height) throws WriterException, IOException {
        byte[] imageBytes = generateQRCode(publicId, width, height);
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        return "data:image/png;base64," + base64;
    }

    /**
     * Build the public URL for a user's allergy passport.
     *
     * @param publicId The user's public ID
     * @return The full public URL
     */
    public String buildPublicUrl(String publicId) {
        return baseUrl + "/u/" + publicId;
    }

    /**
     * Get the configured base URL.
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}
