package com.allergypassport.controller;

import com.allergypassport.dto.AllergyRequest;
import com.allergypassport.dto.ProfileRequest;
import com.allergypassport.dto.ScanResult;
import com.allergypassport.entity.AllergySeverity;
import com.allergypassport.entity.AllergyType;
import com.allergypassport.entity.User;
import com.allergypassport.entity.UserAllergy;
import com.allergypassport.service.AllergenScannerService;
import com.allergypassport.service.CustomOAuth2User;
import com.allergypassport.service.UserService;
import com.allergypassport.util.QRCodeService;
import com.google.zxing.WriterException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller for HTMX-powered interactions and API endpoints.
 * Handles allergy CRUD operations and profile updates.
 */
@Controller
@RequestMapping("/api")
public class AllergyController {

    private static final Logger log = LoggerFactory.getLogger(AllergyController.class);

    private final UserService userService;
    private final QRCodeService qrCodeService;
    private final AllergenScannerService scannerService;

    public AllergyController(UserService userService,
                            QRCodeService qrCodeService,
                            @Autowired(required = false) AllergenScannerService scannerService) {
        this.userService = userService;
        this.qrCodeService = qrCodeService;
        this.scannerService = scannerService;
    }

    // ==================== ALLERGY MANAGEMENT ====================

    /**
     * Get the allergy list fragment (for HTMX updates).
     */
    @GetMapping("/allergies")
    public String getAllergies(@AuthenticationPrincipal CustomOAuth2User principal, Model model) {
        List<UserAllergy> allergies = userService.getUserAllergies(principal.getUserId());
        model.addAttribute("allergies", allergies);
        return "fragments/allergies :: allergyList";
    }

    /**
     * Add a new allergy (HTMX form submission).
     */
    @PostMapping("/allergies")
    public String addAllergy(@AuthenticationPrincipal CustomOAuth2User principal,
                             @RequestParam("allergyType") AllergyType allergyType,
                             @RequestParam("severity") AllergySeverity severity,
                             @RequestParam(value = "notes", required = false) String notes,
                             Model model) {
        try {
            userService.saveAllergy(principal.getUserId(), allergyType, severity, notes);
            log.info("User {} added allergy: {}", principal.getUserId(), allergyType);
        } catch (Exception e) {
            log.error("Failed to add allergy for user {}", principal.getUserId(), e);
            model.addAttribute("error", "Failed to add allergy: " + e.getMessage());
        }

        // Return updated allergy list
        List<UserAllergy> allergies = userService.getUserAllergies(principal.getUserId());
        model.addAttribute("allergies", allergies);
        return "fragments/allergies :: allergyList";
    }

    /**
     * Update an existing allergy.
     */
    @PutMapping("/allergies/{id}")
    public String updateAllergy(@AuthenticationPrincipal CustomOAuth2User principal,
                                @PathVariable Long id,
                                @RequestParam("severity") AllergySeverity severity,
                                @RequestParam(value = "notes", required = false) String notes,
                                Model model) {
        try {
            // Get the existing allergy to get its type
            UserAllergy existing = userService.getUserAllergies(principal.getUserId())
                    .stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Allergy not found"));

            userService.saveAllergy(principal.getUserId(), existing.getAllergyType(), severity, notes);
            log.info("User {} updated allergy: {}", principal.getUserId(), id);
        } catch (Exception e) {
            log.error("Failed to update allergy {} for user {}", id, principal.getUserId(), e);
            model.addAttribute("error", "Failed to update allergy: " + e.getMessage());
        }

        List<UserAllergy> allergies = userService.getUserAllergies(principal.getUserId());
        model.addAttribute("allergies", allergies);
        return "fragments/allergies :: allergyList";
    }

    /**
     * Delete an allergy (HTMX delete).
     */
    @DeleteMapping("/allergies/{id}")
    public String deleteAllergy(@AuthenticationPrincipal CustomOAuth2User principal,
                                @PathVariable Long id,
                                Model model) {
        try {
            userService.removeAllergyById(principal.getUserId(), id);
            log.info("User {} deleted allergy: {}", principal.getUserId(), id);
        } catch (Exception e) {
            log.error("Failed to delete allergy {} for user {}", id, principal.getUserId(), e);
            model.addAttribute("error", "Failed to delete allergy: " + e.getMessage());
        }

        List<UserAllergy> allergies = userService.getUserAllergies(principal.getUserId());
        model.addAttribute("allergies", allergies);
        return "fragments/allergies :: allergyList";
    }

    /**
     * Get the add allergy form modal content.
     */
    @GetMapping("/allergies/form")
    public String getAllergyForm(@AuthenticationPrincipal CustomOAuth2User principal, Model model) {
        // Get already selected allergies to filter them out
        List<UserAllergy> existingAllergies = userService.getUserAllergies(principal.getUserId());
        List<AllergyType> selectedTypes = existingAllergies.stream()
                .map(UserAllergy::getAllergyType)
                .toList();

        model.addAttribute("allAllergyTypes", AllergyType.values());
        model.addAttribute("selectedTypes", selectedTypes);
        model.addAttribute("severities", AllergySeverity.values());
        return "fragments/allergies :: addAllergyForm";
    }

    /**
     * Get edit form for a specific allergy.
     */
    @GetMapping("/allergies/{id}/edit")
    public String getEditAllergyForm(@AuthenticationPrincipal CustomOAuth2User principal,
                                     @PathVariable Long id,
                                     Model model) {
        UserAllergy allergy = userService.getUserAllergies(principal.getUserId())
                .stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Allergy not found"));

        model.addAttribute("allergy", allergy);
        model.addAttribute("severities", AllergySeverity.values());
        return "fragments/allergies :: editAllergyForm";
    }

    // ==================== PROFILE MANAGEMENT ====================

    /**
     * Update user profile (HTMX form).
     */
    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomOAuth2User principal,
                                @RequestParam("displayName") String displayName,
                                @RequestParam(value = "bio", required = false) String bio,
                                Model model) {
        try {
            User user = userService.updateProfile(principal.getUserId(), displayName, bio);
            model.addAttribute("user", user);
            model.addAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            log.error("Failed to update profile for user {}", principal.getUserId(), e);
            model.addAttribute("error", "Failed to update profile: " + e.getMessage());
        }

        return "fragments/profile :: profileForm";
    }

    /**
     * Upload profile picture.
     */
    @PostMapping("/profile/picture")
    public String uploadProfilePicture(@AuthenticationPrincipal CustomOAuth2User principal,
                                        @RequestParam("picture") MultipartFile file,
                                        Model model) {
        try {
            User user = userService.uploadProfilePicture(principal.getUserId(), file);
            model.addAttribute("user", user);
            model.addAttribute("success", "Profile picture updated");
        } catch (Exception e) {
            log.error("Failed to upload profile picture for user {}", principal.getUserId(), e);
            model.addAttribute("error", "Failed to upload picture: " + e.getMessage());
        }

        return "fragments/profile :: profilePicture";
    }

    /**
     * Delete profile picture (revert to Google picture).
     */
    @DeleteMapping("/profile/picture")
    public String deleteProfilePicture(@AuthenticationPrincipal CustomOAuth2User principal,
                                        Model model) {
        try {
            User user = userService.deleteProfilePicture(principal.getUserId());
            model.addAttribute("user", user);
            model.addAttribute("success", "Profile picture removed");
        } catch (Exception e) {
            log.error("Failed to delete profile picture for user {}", principal.getUserId(), e);
            model.addAttribute("error", "Failed to remove picture: " + e.getMessage());
        }

        return "fragments/profile :: profilePicture";
    }

    // ==================== QR CODE ====================

    /**
     * Generate QR code image for the current user.
     */
    @GetMapping("/qr")
    public ResponseEntity<byte[]> getQRCode(@AuthenticationPrincipal CustomOAuth2User principal,
                                            @RequestParam(value = "size", defaultValue = "300") int size) {
        try {
            byte[] qrCode = qrCodeService.generateQRCode(principal.getPublicId(), size, size);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qrCode);
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code for user {}", principal.getUserId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== ALLERGEN SCANNER ====================

    /**
     * Scan an image for allergens using OCR (HTMX form submission).
     * Returns scan results fragment with detected allergens.
     */
    @PostMapping("/scan-allergens")
    public String scanAllergens(@AuthenticationPrincipal CustomOAuth2User principal,
                                @RequestParam("image") MultipartFile file,
                                Model model) {
        try {
            // Check if scanner is available
            if (scannerService == null) {
                model.addAttribute("error", "Allergen scanner is not configured on this server");
                return "fragments/scan-result :: scanError";
            }

            // Validate file
            if (file.isEmpty()) {
                model.addAttribute("error", "Please select an image to scan");
                return "fragments/scan-result :: scanError";
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                model.addAttribute("error", "File must be an image (JPEG, PNG, or WebP)");
                return "fragments/scan-result :: scanError";
            }

            // Check file size (max 10MB)
            long maxSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxSize) {
                model.addAttribute("error", "Image size must be less than 10MB");
                return "fragments/scan-result :: scanError";
            }

            log.info("User {} scanning image: {} ({} bytes, type: {})",
                    principal.getUserId(), file.getOriginalFilename(), file.getSize(), contentType);

            // Get user's allergies
            List<UserAllergy> userAllergies = userService.getUserAllergies(principal.getUserId());
            if (userAllergies == null || userAllergies.isEmpty()) {
                model.addAttribute("error", "You need to register at least one allergy before using the scanner");
                return "fragments/scan-result :: scanError";
            }

            // Perform scan
            byte[] imageData = file.getBytes();
            Set<UserAllergy> allergySet = new HashSet<>(userAllergies);
            ScanResult scanResult = scannerService.scanForAllergens(imageData, allergySet);

            // Add result to model
            model.addAttribute("scanResult", scanResult);

            log.info("Scan completed for user {}: {} allergen(s) detected",
                    principal.getUserId(), scanResult.detections().size());

            return "fragments/scan-result :: scanSuccess";

        } catch (IllegalStateException e) {
            log.error("Scanner not configured: {}", e.getMessage());
            model.addAttribute("error", "Scanner is not properly configured. Please contact support.");
            return "fragments/scan-result :: scanError";
        } catch (IOException e) {
            log.error("Failed to read image file for user {}", principal.getUserId(), e);
            model.addAttribute("error", "Failed to read image file");
            return "fragments/scan-result :: scanError";
        } catch (Exception e) {
            log.error("Failed to scan image for user {}", principal.getUserId(), e);
            model.addAttribute("error", "Scanning failed: " + e.getMessage());
            return "fragments/scan-result :: scanError";
        }
    }
}
