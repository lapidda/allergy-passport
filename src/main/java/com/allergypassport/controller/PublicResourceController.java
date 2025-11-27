package com.allergypassport.controller;

import com.allergypassport.entity.User;
import com.allergypassport.repository.UserRepository;
import com.allergypassport.util.QRCodeService;
import com.google.zxing.WriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;

/**
 * Controller for public resources (QR codes, profile pictures).
 * These endpoints don't require authentication.
 */
@RestController
public class PublicResourceController {

    private static final Logger log = LoggerFactory.getLogger(PublicResourceController.class);

    private final UserRepository userRepository;
    private final QRCodeService qrCodeService;

    public PublicResourceController(UserRepository userRepository, QRCodeService qrCodeService) {
        this.userRepository = userRepository;
        this.qrCodeService = qrCodeService;
    }

    /**
     * Public QR code endpoint - generates QR code for a user's public page.
     */
    @GetMapping("/qr/{publicId}")
    public ResponseEntity<byte[]> getPublicQRCode(@PathVariable String publicId,
                                                   @RequestParam(value = "size", defaultValue = "300") int size) {
        // Validate user exists
        if (!userRepository.findByPublicId(publicId).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // Limit size for security
        int clampedSize = Math.min(Math.max(size, 100), 1000);

        try {
            byte[] qrCode = qrCodeService.generateQRCode(publicId, clampedSize, clampedSize);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .cacheControl(CacheControl.maxAge(Duration.ofHours(1)))
                    .body(qrCode);
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code for public ID: {}", publicId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Public profile picture endpoint.
     * Returns the user's custom profile picture or a redirect to their Google picture.
     */
    @GetMapping("/profile-picture/{publicId}")
    @Transactional(readOnly = true)  // Required for PostgreSQL LOB access
    public ResponseEntity<?> getProfilePicture(@PathVariable String publicId) {
        User user = userRepository.findByPublicId(publicId).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Return custom profile picture if available
        if (user.hasCustomProfilePicture()) {
            MediaType mediaType = MediaType.parseMediaType(
                    user.getProfilePictureContentType() != null 
                            ? user.getProfilePictureContentType() 
                            : "image/jpeg"
            );

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(30)))
                    .body(user.getProfilePicture());
        }

        // Redirect to Google profile picture
        if (user.getGooglePictureUrl() != null) {
            return ResponseEntity
                    .status(302)
                    .header("Location", user.getGooglePictureUrl())
                    .build();
        }

        // No picture available
        return ResponseEntity.notFound().build();
    }

    /**
     * Download QR code as attachment.
     */
    @GetMapping("/qr/{publicId}/download")
    public ResponseEntity<byte[]> downloadQRCode(@PathVariable String publicId,
                                                  @RequestParam(value = "size", defaultValue = "500") int size) {
        if (!userRepository.findByPublicId(publicId).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        int clampedSize = Math.min(Math.max(size, 100), 1000);

        try {
            byte[] qrCode = qrCodeService.generateQRCode(publicId, clampedSize, clampedSize);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header("Content-Disposition", "attachment; filename=\"allergy-passport-qr.png\"")
                    .body(qrCode);
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code for download: {}", publicId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
