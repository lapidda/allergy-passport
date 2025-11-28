package com.allergypassport.service;

import com.allergypassport.dto.OcrResult;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Google Cloud Vision API implementation for OCR text extraction.
 * <p>
 * Uses Google Cloud Vision API to:
 * - Extract text from images using OCR
 * - Detect the language of extracted text
 * - Provide confidence scores
 * <p>
 * Cost Information:
 * - ~$1.50 per 1000 images for TEXT_DETECTION
 * - No caching needed (ephemeral results)
 */
@Service
@ConditionalOnProperty(name = "vision.provider", havingValue = "google-cloud", matchIfMissing = false)
public class GoogleCloudVisionService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCloudVisionService.class);

    private final String projectId;

    public GoogleCloudVisionService(@Value("${google.cloud.project-id:}") String projectId) {
        this.projectId = projectId;

        if (projectId == null || projectId.isBlank()) {
            log.warn("Google Cloud Project ID not configured. OCR scanning will not work.");
        }
    }

    /**
     * Perform OCR on an image and extract text with language detection.
     *
     * @param imageData The image data as byte array
     * @return OcrResult containing extracted text, detected language, and confidence
     * @throws IllegalStateException if Vision API is not properly configured
     * @throws RuntimeException if OCR processing fails
     */
    public OcrResult performOcr(byte[] imageData) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException("Google Cloud Project ID not configured");
        }

        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("Image data cannot be empty");
        }

        log.info("Starting OCR processing for image ({} bytes)", imageData.length);

        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
            // Build the image request
            ByteString byteString = ByteString.copyFrom(imageData);
            Image image = Image.newBuilder().setContent(byteString).build();

            // Create features for text detection and language hints
            Feature textDetectionFeature = Feature.newBuilder()
                    .setType(Feature.Type.TEXT_DETECTION)
                    .build();

            // Build the annotation request
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(textDetectionFeature)
                    .setImage(image)
                    .build();

            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);

            // Perform the request
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            if (responses.isEmpty()) {
                log.warn("No response from Vision API");
                return new OcrResult("", "unknown", 0.0f);
            }

            AnnotateImageResponse res = responses.get(0);

            // Check for errors
            if (res.hasError()) {
                log.error("Vision API error: {}", res.getError().getMessage());
                throw new RuntimeException("Vision API error: " + res.getError().getMessage());
            }

            // Extract text annotations
            if (res.getTextAnnotationsList().isEmpty()) {
                log.info("No text detected in image");
                return new OcrResult("", "unknown", 0.0f);
            }

            // The first annotation contains all detected text
            EntityAnnotation textAnnotation = res.getTextAnnotations(0);
            String detectedText = textAnnotation.getDescription();

            // Extract language from locale if available
            String languageCode = "unknown";
            float confidence = 1.0f; // Default to high confidence

            if (!textAnnotation.getLocale().isEmpty()) {
                languageCode = normalizeLangCode(textAnnotation.getLocale());
            }

            // Get confidence from bounding box accuracy (use first annotation score)
            if (textAnnotation.getScore() > 0) {
                confidence = textAnnotation.getScore();
            }

            log.info("OCR successful: detected {} characters in language '{}' (confidence: {:.2f})",
                    detectedText.length(), languageCode, confidence);

            return new OcrResult(detectedText, languageCode, confidence);

        } catch (Exception e) {
            log.error("OCR processing failed: {}", e.getMessage(), e);
            throw new RuntimeException("OCR processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Normalize language code to standard format.
     * Handles cases like "en_US" -> "en", "zh-Hans" -> "zh", etc.
     */
    private String normalizeLangCode(String langCode) {
        if (langCode == null || langCode.isBlank()) {
            return "unknown";
        }

        // Handle locale formats like "en_US", "de_DE"
        if (langCode.contains("_")) {
            langCode = langCode.split("_")[0];
        }

        // Handle BCP 47 language tags with hyphens
        if (langCode.contains("-")) {
            langCode = langCode.split("-")[0];
        }

        // Return lowercase base language code
        return langCode.toLowerCase();
    }

    /**
     * Check if the Vision API is properly configured.
     */
    public boolean isConfigured() {
        return projectId != null && !projectId.isBlank();
    }
}
