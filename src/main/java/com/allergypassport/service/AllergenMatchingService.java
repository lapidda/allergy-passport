package com.allergypassport.service;

import com.allergypassport.dto.DetectedAllergen;
import com.allergypassport.entity.AllergyType;
import com.allergypassport.entity.UserAllergy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Service for matching allergen keywords in text across multiple languages.
 * Loads keyword mappings from JSON files and performs case-insensitive matching.
 */
@Service
public class AllergenMatchingService {

    private static final Logger log = LoggerFactory.getLogger(AllergenMatchingService.class);

    private final ObjectMapper objectMapper;
    private final Map<AllergyType, Map<String, List<String>>> allergenKeywords;

    public AllergenMatchingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.allergenKeywords = new HashMap<>();
    }

    /**
     * Load allergen keyword mappings from JSON files on startup.
     */
    @PostConstruct
    public void loadKeywords() {
        log.info("Loading allergen keyword mappings...");

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:allergen-keywords/*.json");

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null) continue;

                // Extract allergen type from filename (e.g., "peanuts.json" -> "PEANUTS")
                String allergenName = filename.replace(".json", "").toUpperCase();

                try {
                    AllergyType allergyType = AllergyType.valueOf(allergenName);

                    // Load keyword map from JSON
                    Map<String, List<String>> keywords = objectMapper.readValue(
                            resource.getInputStream(),
                            new TypeReference<Map<String, List<String>>>() {}
                    );

                    allergenKeywords.put(allergyType, keywords);

                    int totalKeywords = keywords.values().stream()
                            .mapToInt(List::size)
                            .sum();

                    log.debug("Loaded {} keywords for {} across {} languages",
                            totalKeywords, allergyType, keywords.size());

                } catch (IllegalArgumentException e) {
                    log.warn("Unknown allergen type in filename: {}", filename);
                } catch (IOException e) {
                    log.error("Failed to load keywords from {}: {}", filename, e.getMessage());
                }
            }

            log.info("Successfully loaded keyword mappings for {} allergen types", allergenKeywords.size());

        } catch (IOException e) {
            log.error("Failed to load allergen keyword files: {}", e.getMessage(), e);
        }
    }

    /**
     * Find allergens in the given text that match the user's allergies.
     *
     * @param text The OCR text to search in
     * @param language The detected language of the text (e.g., "en", "de", "fr")
     * @param userAllergies The set of allergies to search for
     * @return List of detected allergens with matched keywords
     */
    public List<DetectedAllergen> findAllergens(String text, String language, Set<UserAllergy> userAllergies) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        if (userAllergies == null || userAllergies.isEmpty()) {
            log.debug("No user allergies to search for");
            return Collections.emptyList();
        }

        List<DetectedAllergen> detections = new ArrayList<>();
        String normalizedText = text.toLowerCase();

        // Normalize language code (e.g., "en-US" -> "en", "unknown" -> fallback to multiple languages)
        String langCode = normalizeLangCode(language);

        log.debug("Searching for allergens in text (language: {})", langCode);

        for (UserAllergy userAllergy : userAllergies) {
            AllergyType allergyType = userAllergy.getAllergyType();
            Map<String, List<String>> keywordMap = allergenKeywords.get(allergyType);

            if (keywordMap == null) {
                log.warn("No keyword mapping found for {}", allergyType);
                continue;
            }

            // Get keywords for the detected language, or try English as fallback
            List<String> keywords = keywordMap.get(langCode);
            if (keywords == null || keywords.isEmpty()) {
                keywords = keywordMap.get("en"); // Fallback to English
                if (keywords == null) {
                    log.debug("No keywords found for {} in language {}", allergyType, langCode);
                    continue;
                }
            }

            // Search for each keyword in the text
            for (String keyword : keywords) {
                if (keyword == null || keyword.isBlank()) continue;

                String lowerKeyword = keyword.toLowerCase();

                // For compound-word languages (German, Dutch, Nordic languages), we need to match
                // keywords within compound words (e.g., "milch" in "VOLLMILCHSCHOKOLADE")
                // For other languages, use word boundaries to avoid false positives
                boolean found = false;

                if (isCompoundWordLanguage(langCode)) {
                    // Simple substring matching for compound word languages
                    // This allows "milch" to match "vollmilchschokolade"
                    found = normalizedText.contains(lowerKeyword);
                } else {
                    // Use word boundary matching for languages without compound words
                    // e.g., "peanut" should not match "peanu" but should match "peanuts"
                    Pattern pattern = Pattern.compile("\\b" + Pattern.quote(lowerKeyword) + "s?\\b");
                    found = pattern.matcher(normalizedText).find();
                }

                if (found) {
                    log.info("Allergen detected: {} (keyword: '{}')", allergyType, keyword);

                    DetectedAllergen detection = new DetectedAllergen(
                            allergyType,
                            keyword,
                            userAllergy.getSeverity()
                    );

                    detections.add(detection);
                    break; // Only add one detection per allergen type
                }
            }
        }

        log.info("Found {} allergen matches in text", detections.size());
        return detections;
    }

    /**
     * Check if a language commonly uses compound words where allergen keywords
     * might be embedded within larger words.
     *
     * @param langCode The normalized language code (e.g., "de", "nl", "sv")
     * @return true if the language uses compound words, false otherwise
     */
    private boolean isCompoundWordLanguage(String langCode) {
        // Germanic and Nordic languages that frequently use compound words
        return langCode.equals("de") ||  // German: vollmilchschokolade, butterreinfett
               langCode.equals("nl") ||  // Dutch: vollemelkchocolade
               langCode.equals("sv") ||  // Swedish: helmjölkschoklad
               langCode.equals("da") ||  // Danish: fuldmælkschokolade
               langCode.equals("no") ||  // Norwegian: helmelkssjokolade
               langCode.equals("fi");    // Finnish: täysmaitosuklaata
    }

    /**
     * Normalize language code to base language.
     * Examples: "en-US" -> "en", "zh-CN" -> "zh", "unknown" -> "en"
     */
    private String normalizeLangCode(String langCode) {
        if (langCode == null || langCode.isBlank() || "unknown".equalsIgnoreCase(langCode)) {
            return "en"; // Default to English for unknown languages
        }

        // Handle locale formats like "en_US", "de_DE"
        if (langCode.contains("_")) {
            langCode = langCode.split("_")[0];
        }

        // Handle BCP 47 language tags with hyphens
        if (langCode.contains("-")) {
            langCode = langCode.split("-")[0];
        }

        return langCode.toLowerCase();
    }

    /**
     * Get the number of loaded allergen types.
     */
    public int getLoadedAllergenCount() {
        return allergenKeywords.size();
    }

    /**
     * Check if keywords are loaded for a specific allergen type.
     */
    public boolean hasKeywords(AllergyType allergyType) {
        return allergenKeywords.containsKey(allergyType);
    }
}
