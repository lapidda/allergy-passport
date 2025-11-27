package com.allergypassport.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Cache for storing translations from Google Cloud Translation API.
 * This reduces API calls and costs by storing translated text.
 */
@Entity
@Table(name = "translation_cache", indexes = {
    @Index(name = "idx_translation_lookup", columnList = "source_text_hash, source_lang, target_lang")
})
public class TranslationCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hash of the source text (for indexing and uniqueness).
     * We store hash instead of full text for better index performance.
     */
    @Column(name = "source_text_hash", nullable = false, length = 64)
    private String sourceTextHash;

    /**
     * The original source text to translate.
     */
    @Column(name = "source_text", nullable = false, columnDefinition = "TEXT")
    private String sourceText;

    /**
     * Source language code (e.g., "en", "de", "fr").
     */
    @Column(name = "source_lang", nullable = false, length = 10)
    private String sourceLang;

    /**
     * Target language code (e.g., "en", "de", "fr").
     */
    @Column(name = "target_lang", nullable = false, length = 10)
    private String targetLang;

    /**
     * The translated text from Google Cloud Translation API.
     */
    @Column(name = "translated_text", nullable = false, columnDefinition = "TEXT")
    private String translatedText;

    /**
     * Timestamp when this translation was cached.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when this translation was last accessed.
     * Used for cache eviction strategy.
     */
    @Column(name = "last_accessed", nullable = false)
    private LocalDateTime lastAccessed;

    /**
     * Number of times this translation has been accessed.
     * Used for analytics and cache optimization.
     */
    @Column(name = "access_count", nullable = false)
    private Long accessCount = 0L;

    public TranslationCache() {
        this.createdAt = LocalDateTime.now();
        this.lastAccessed = LocalDateTime.now();
    }

    public TranslationCache(String sourceTextHash, String sourceText, String sourceLang,
                           String targetLang, String translatedText) {
        this();
        this.sourceTextHash = sourceTextHash;
        this.sourceText = sourceText;
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
        this.translatedText = translatedText;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceTextHash() {
        return sourceTextHash;
    }

    public void setSourceTextHash(String sourceTextHash) {
        this.sourceTextHash = sourceTextHash;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getSourceLang() {
        return sourceLang;
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    public String getTargetLang() {
        return targetLang;
    }

    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(LocalDateTime lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public Long getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(Long accessCount) {
        this.accessCount = accessCount;
    }

    public void incrementAccessCount() {
        this.accessCount++;
        this.lastAccessed = LocalDateTime.now();
    }
}
