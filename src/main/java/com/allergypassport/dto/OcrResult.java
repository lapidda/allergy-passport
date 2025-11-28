package com.allergypassport.dto;

/**
 * DTO representing the result of OCR text extraction from an image.
 *
 * @param detectedText The extracted text from the image
 * @param languageCode The detected language code (e.g., "en", "de", "fr")
 * @param confidence The confidence score of the text detection (0.0 to 1.0)
 */
public record OcrResult(
        String detectedText,
        String languageCode,
        float confidence
) {}
