package com.allergypassport.controller;

import com.allergypassport.dto.AllergyRequest;
import com.allergypassport.dto.ProfileRequest;
import com.allergypassport.entity.AllergySeverity;
import com.allergypassport.entity.AllergyType;
import com.allergypassport.entity.User;
import com.allergypassport.entity.UserAllergy;
import com.allergypassport.service.CustomOAuth2User;
import com.allergypassport.service.UserService;
import com.allergypassport.util.QRCodeService;
import com.google.zxing.WriterException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    public AllergyController(UserService userService, QRCodeService qrCodeService) {
        this.userService = userService;
        this.qrCodeService = qrCodeService;
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
}
