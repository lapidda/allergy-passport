package com.allergypassport.service;

import com.allergypassport.dto.DetectedAllergen;
import com.allergypassport.dto.OcrResult;
import com.allergypassport.dto.ScanResult;
import com.allergypassport.entity.UserAllergy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Orchestration service for allergen scanning.
 * Combines OCR text extraction with allergen keyword matching.
 */
@Service
@ConditionalOnBean(GoogleCloudVisionService.class)
public class AllergenScannerService {

    private static final Logger log = LoggerFactory.getLogger(AllergenScannerService.class);

    private final GoogleCloudVisionService visionService;
    private final AllergenMatchingService matchingService;

    public AllergenScannerService(GoogleCloudVisionService visionService,
                                  AllergenMatchingService matchingService) {
        this.visionService = visionService;
        this.matchingService = matchingService;
    }

    /**
     * Scan an image for allergens that match the user's registered allergies.
     *
     * @param imageData The image data as byte array
     * @param userAllergies The set of user's registered allergies to search for
     * @return ScanResult containing detection results and OCR text
     * @throws IllegalStateException if Vision API is not configured
     * @throws RuntimeException if OCR processing fails
     */
    public ScanResult scanForAllergens(byte[] imageData, Set<UserAllergy> userAllergies) {
        log.info("Starting allergen scan for user with {} registered allergies",
                userAllergies != null ? userAllergies.size() : 0);

        // Validate inputs
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("Image data cannot be empty");
        }

        if (userAllergies == null || userAllergies.isEmpty()) {
            log.warn("No user allergies to scan for");
            return new ScanResult(false, List.of(), "unknown", "");
        }

        // Step 1: Perform OCR to extract text from image
        OcrResult ocrResult;
        try {
            ocrResult = visionService.performOcr(imageData);
        } catch (Exception e) {
            log.error("OCR failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract text from image: " + e.getMessage(), e);
        }

        String detectedText = ocrResult.detectedText();
        String languageCode = ocrResult.languageCode();

        log.info("OCR completed: {} characters extracted, language: {}, confidence: {:.2f}",
                detectedText.length(), languageCode, ocrResult.confidence());

        // Handle case where no text was detected
        if (detectedText == null || detectedText.isBlank()) {
            log.info("No text detected in image");
            return new ScanResult(false, List.of(), languageCode, "");
        }

        // Step 2: Search for allergen keywords in the extracted text
        List<DetectedAllergen> detections = matchingService.findAllergens(
                detectedText,
                languageCode,
                userAllergies
        );

        boolean allergensFound = !detections.isEmpty();

        if (allergensFound) {
            log.warn("ALLERGEN ALERT: Found {} allergen(s) in scanned image", detections.size());
            detections.forEach(d -> log.warn("  - {} (keyword: '{}')", d.allergyType(), d.matchedKeyword()));
        } else {
            log.info("No allergens detected in scanned image");
        }

        return new ScanResult(
                allergensFound,
                detections,
                languageCode,
                detectedText
        );
    }

    /**
     * Check if the scanner service is properly configured and ready to use.
     */
    public boolean isConfigured() {
        return visionService.isConfigured() && matchingService.getLoadedAllergenCount() > 0;
    }

    /**
     * Get service status information for debugging.
     */
    public String getServiceStatus() {
        return String.format(
                "Vision API: %s, Allergen Keywords: %d types loaded",
                visionService.isConfigured() ? "configured" : "not configured",
                matchingService.getLoadedAllergenCount()
        );
    }
}
