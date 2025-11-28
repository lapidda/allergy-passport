package com.allergypassport.dto;

import java.util.List;

/**
 * DTO representing the complete result of an allergen scan operation.
 *
 * @param allergensFound Whether any allergens were detected in the scan
 * @param detections List of detected allergens with details
 * @param detectedLanguage The detected language of the OCR text
 * @param originalText The original OCR text extracted from the image
 */
public record ScanResult(
        boolean allergensFound,
        List<DetectedAllergen> detections,
        String detectedLanguage,
        String originalText
) {}
