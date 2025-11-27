package com.allergypassport.service;

import java.util.Locale;

/**
 * Service interface for translating user-provided text.
 * This is designed to be pluggable - you can implement this with DeepL, OpenAI, or other translation APIs.
 */
public interface TranslationService {

    /**
     * Translate the given text to the target locale.
     *
     * @param text         The text to translate
     * @param sourceLocale The source language (can be null for auto-detection)
     * @param targetLocale The target language
     * @return The translated text
     */
    String translate(String text, Locale sourceLocale, Locale targetLocale);

    /**
     * Translate the given text to the target locale with auto-detection of source language.
     *
     * @param text         The text to translate
     * @param targetLocale The target language
     * @return The translated text
     */
    default String translate(String text, Locale targetLocale) {
        return translate(text, null, targetLocale);
    }

    /**
     * Check if translation is supported for the given locale.
     *
     * @param locale The locale to check
     * @return true if translation is supported
     */
    boolean isSupported(Locale locale);

    /**
     * Get the name of the translation provider.
     *
     * @return Provider name (e.g., "Mock", "DeepL", "OpenAI")
     */
    String getProviderName();

    /**
     * Detect the language of the given text.
     * Returns a normalized language code (e.g., "en", "es", "zh-CN") or null if detection fails.
     *
     * @param text The text to detect the language of
     * @return The detected language code or null if detection fails
     */
    String detectLanguage(String text);
}
