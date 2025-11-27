package com.allergypassport.service.impl;

import com.allergypassport.service.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

/**
 * Mock implementation of TranslationService.
 * Returns the original text unchanged - designed to be replaced with a real implementation.
 * 
 * To implement a real translation service (e.g., DeepL or OpenAI), create a new class
 * that implements TranslationService and mark it as @Primary, or use @ConditionalOnProperty
 * to switch between implementations.
 * 
 * Example for DeepL:
 * <pre>
 * @Service
 * @ConditionalOnProperty(name = "app.translation.provider", havingValue = "deepl")
 * public class DeepLTranslationService implements TranslationService {
 *     // Implementation using DeepL API
 * }
 * </pre>
 */
@Service
public class MockTranslationService implements TranslationService {

    private static final Logger log = LoggerFactory.getLogger(MockTranslationService.class);

    private static final Set<Locale> SUPPORTED_LOCALES = Set.of(
            Locale.ENGLISH,
            Locale.GERMAN,
            Locale.FRENCH,
            Locale.ITALIAN,
            new Locale("es"),  // Spanish
            new Locale("pt"),  // Portuguese
            new Locale("nl"),  // Dutch
            new Locale("pl"),  // Polish
            new Locale("ja"),  // Japanese
            new Locale("zh")   // Chinese
    );

    @Override
    public String translate(String text, Locale sourceLocale, Locale targetLocale) {
        if (text == null || text.isBlank()) {
            return text;
        }

        log.debug("Mock translation requested: '{}' from {} to {}", 
                  text.substring(0, Math.min(50, text.length())),
                  sourceLocale, 
                  targetLocale);

        // Mock implementation - just returns the original text
        // Replace this with actual API call in a real implementation
        return text;
    }

    @Override
    public boolean isSupported(Locale locale) {
        return SUPPORTED_LOCALES.contains(locale) || 
               SUPPORTED_LOCALES.stream()
                   .anyMatch(l -> l.getLanguage().equals(locale.getLanguage()));
    }

    @Override
    public String getProviderName() {
        return "Mock (No Translation)";
    }

    @Override
    public String detectLanguage(String text) {
        // Mock implementation - no actual language detection
        // Returns null to indicate detection is not available
        log.debug("Mock language detection requested for text of length {}",
                  text != null ? text.length() : 0);
        return null;
    }
}
