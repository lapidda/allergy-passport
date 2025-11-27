package com.allergypassport.service;

import com.allergypassport.entity.TranslationCache;
import com.allergypassport.repository.TranslationCacheRepository;
import com.google.cloud.translate.v3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * Google Cloud Translation API implementation with database caching.
 * <p>
 * Cost Optimization Strategy:
 * - Caches all translations in database
 * - Only calls API for new text
 * - Uses SHA-256 hash for efficient cache lookups
 * <p>
 * Supported Languages (20 most relevant for restaurants):
 * en, es, fr, de, it, pt, ru, zh-CN, ja, ko, ar, tr, nl, pl, sv, da, no, fi, el, hi
 */
@Service
@Primary
@ConditionalOnProperty(name = "translation.provider", havingValue = "google-cloud", matchIfMissing = false)
public class GoogleCloudTranslationService implements TranslationService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCloudTranslationService.class);

    private final TranslationCacheRepository translationCacheRepository;
    private final String projectId;

    public GoogleCloudTranslationService(TranslationCacheRepository translationCacheRepository,
                                        @Value("${google.cloud.project-id:}") String projectId) {
        this.translationCacheRepository = translationCacheRepository;
        this.projectId = projectId;

        if (projectId == null || projectId.isBlank()) {
            log.warn("Google Cloud Project ID not configured. Translation will not work.");
        }
    }

    @Override
    @Transactional
    public String translate(String text, Locale sourceLocale, Locale targetLocale) {
        // Return original text if empty
        if (text == null || text.isBlank()) {
            return text;
        }

        // Normalize language codes
        String sourceLang = sourceLocale != null ? normalizeLangCode(sourceLocale.getLanguage()) : null;
        String targetLang = normalizeLangCode(targetLocale.getLanguage());

        // Return original if same language
        if (sourceLang != null && sourceLang.equals(targetLang)) {
            return text;
        }

        // Check cache first
        String textHash = hashText(text);
        String cacheSourceLang = sourceLang != null ? sourceLang : "auto";

        Optional<TranslationCache> cached = translationCacheRepository
                .findBySourceTextHashAndSourceLangAndTargetLang(textHash, cacheSourceLang, targetLang);

        if (cached.isPresent()) {
            log.debug("Translation cache HIT for {} -> {}", cacheSourceLang, targetLang);
            TranslationCache translation = cached.get();
            translation.incrementAccessCount();
            translationCacheRepository.save(translation);
            return translation.getTranslatedText();
        }

        // Translation not in cache - check if API is configured
        if (projectId == null || projectId.isBlank()) {
            log.warn("Google Cloud Project ID not configured. Returning original text.");
            return text;
        }

        // Call Google Cloud Translation API
        log.info("Translation cache MISS for {} -> {}. Calling API... (text length: {})",
                cacheSourceLang, targetLang, text.length());
        try {
            String translatedText = callTranslationAPI(text, sourceLang, targetLang);

            // Cache the translation
            TranslationCache newTranslation = new TranslationCache(
                    textHash, text, cacheSourceLang, targetLang, translatedText
            );
            translationCacheRepository.save(newTranslation);

            log.info("Translation successful and cached: {} -> {}", cacheSourceLang, targetLang);

            return translatedText;

        } catch (Exception e) {
            log.error("Translation API error for {} -> {}: {}",
                    cacheSourceLang, targetLang, e.getMessage(), e);
            return text; // Fallback to original text
        }
    }

    /**
     * Call Google Cloud Translation API.
     */
    private String callTranslationAPI(String text, String sourceLang, String targetLang) throws Exception {
        LocationName parent = LocationName.of(projectId, "global");

        try (TranslationServiceClient client = TranslationServiceClient.create()) {
            TranslateTextRequest.Builder requestBuilder = TranslateTextRequest.newBuilder()
                    .setParent(parent.toString())
                    .setMimeType("text/plain")
                    .setTargetLanguageCode(targetLang)
                    .addContents(text);

            // Add source language if provided
            if (sourceLang != null) {
                requestBuilder.setSourceLanguageCode(sourceLang);
            }

            TranslateTextRequest request = requestBuilder.build();
            TranslateTextResponse response = client.translateText(request);

            if (response.getTranslationsCount() > 0) {
                return response.getTranslations(0).getTranslatedText();
            } else {
                throw new RuntimeException("No translation returned from API");
            }
        }
    }

    /**
     * Normalize language code to standard format.
     * Handles cases like "en_US" -> "en", "zh-Hans" -> "zh-CN", etc.
     */
    private String normalizeLangCode(String langCode) {
        if (langCode == null || langCode.isBlank()) {
            return "en";
        }

        // Handle locale formats like "en_US", "de_DE"
        if (langCode.contains("_")) {
            langCode = langCode.split("_")[0];
        }

        // Handle BCP 47 language tags
        langCode = langCode.toLowerCase();

        // Special cases for Chinese
        if (langCode.startsWith("zh")) {
            // Default to Simplified Chinese
            return "zh-CN";
        }

        // Return base language code
        return langCode;
    }

    /**
     * Generate SHA-256 hash of text for cache lookup.
     */
    private String hashText(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            return Integer.toString(text.hashCode());
        }
    }

    @Override
    public boolean isSupported(Locale locale) {
        // Google Cloud Translation supports all these languages
        String lang = normalizeLangCode(locale.getLanguage());
        return true; // Google Cloud supports virtually all languages
    }

    @Override
    public String getProviderName() {
        return "Google Cloud Translation";
    }

    /**
     * Detect the language of the given text.
     * Returns null if detection fails or if the API is not configured.
     */
    public String detectLanguage(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        if (projectId == null || projectId.isBlank()) {
            log.debug("Google Cloud Project ID not configured. Cannot detect language.");
            return null;
        }

        try {
            LocationName parent = LocationName.of(projectId, "global");

            try (TranslationServiceClient client = TranslationServiceClient.create()) {
                DetectLanguageRequest request = DetectLanguageRequest.newBuilder()
                        .setParent(parent.toString())
                        .setMimeType("text/plain")
                        .setContent(text)
                        .build();

                DetectLanguageResponse response = client.detectLanguage(request);

                if (response.getLanguagesCount() > 0) {
                    String detectedLang = response.getLanguages(0).getLanguageCode();
                    float confidence = response.getLanguages(0).getConfidence();
                    log.debug("Detected language: {} (confidence: {:.2f})", detectedLang, confidence);
                    return normalizeLangCode(detectedLang);
                }
            }
        } catch (Exception e) {
            log.warn("Language detection failed: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Get cache statistics (for monitoring).
     */
    public long getCacheSize() {
        return translationCacheRepository.countTotalTranslations();
    }
}
