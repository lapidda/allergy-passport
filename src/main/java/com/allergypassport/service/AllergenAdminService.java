package com.allergypassport.service;

import com.allergypassport.entity.*;
import com.allergypassport.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Admin service for managing allergens, categories, translations, and keywords.
 * Provides methods for CRUD operations and cache management.
 */
@Service
@Transactional
public class AllergenAdminService {

    private static final Logger log = LoggerFactory.getLogger(AllergenAdminService.class);

    private final AllergenRepository allergenRepository;
    private final AllergenCategoryRepository categoryRepository;
    private final AllergenTranslationRepository translationRepository;
    private final AllergenKeywordRepository keywordRepository;
    private final CategoryTranslationRepository categoryTranslationRepository;
    private final CacheManager cacheManager;
    private final AllergenMatchingService matchingService;

    public AllergenAdminService(
            AllergenRepository allergenRepository,
            AllergenCategoryRepository categoryRepository,
            AllergenTranslationRepository translationRepository,
            AllergenKeywordRepository keywordRepository,
            CategoryTranslationRepository categoryTranslationRepository,
            CacheManager cacheManager,
            AllergenMatchingService matchingService) {
        this.allergenRepository = allergenRepository;
        this.categoryRepository = categoryRepository;
        this.translationRepository = translationRepository;
        this.keywordRepository = keywordRepository;
        this.categoryTranslationRepository = categoryTranslationRepository;
        this.cacheManager = cacheManager;
        this.matchingService = matchingService;
    }

    // ==================== ALLERGEN MANAGEMENT ====================

    /**
     * Create a new allergen.
     */
    public Allergen createAllergen(String code, String categoryCode, Integer displayOrder, Boolean isLegallyMandated) {
        AllergenCategory category = categoryRepository.findByCode(categoryCode)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryCode));

        Allergen allergen = new Allergen();
        allergen.setCode(code);
        allergen.setCategory(category);
        allergen.setDisplayOrder(displayOrder);
        allergen.setIsLegallyMandated(isLegallyMandated);

        allergen = allergenRepository.save(allergen);
        log.info("Created new allergen: {}", code);

        clearCaches();
        return allergen;
    }

    /**
     * Update an existing allergen.
     */
    public Allergen updateAllergen(Long allergenId, Integer displayOrder, Boolean isLegallyMandated) {
        Allergen allergen = allergenRepository.findById(allergenId)
                .orElseThrow(() -> new IllegalArgumentException("Allergen not found: " + allergenId));

        if (displayOrder != null) {
            allergen.setDisplayOrder(displayOrder);
        }
        if (isLegallyMandated != null) {
            allergen.setIsLegallyMandated(isLegallyMandated);
        }

        allergen = allergenRepository.save(allergen);
        log.info("Updated allergen: {}", allergen.getCode());

        clearCaches();
        return allergen;
    }

    /**
     * Delete an allergen (only if not used by any users).
     */
    public void deleteAllergen(Long allergenId) {
        Allergen allergen = allergenRepository.findById(allergenId)
                .orElseThrow(() -> new IllegalArgumentException("Allergen not found: " + allergenId));

        // In a real application, you'd check if any users have this allergy first
        allergenRepository.delete(allergen);
        log.info("Deleted allergen: {}", allergen.getCode());

        clearCaches();
    }

    // ==================== TRANSLATION MANAGEMENT ====================

    /**
     * Add or update a translation for an allergen.
     */
    public AllergenTranslation addOrUpdateAllergenTranslation(Long allergenId, String languageCode,
                                                              String name, String description) {
        Allergen allergen = allergenRepository.findById(allergenId)
                .orElseThrow(() -> new IllegalArgumentException("Allergen not found: " + allergenId));

        // Check if translation already exists
        Optional<AllergenTranslation> existingOpt = translationRepository.findAll().stream()
                .filter(t -> t.getAllergen().getId().equals(allergenId) &&
                            t.getLanguageCode().equals(languageCode))
                .findFirst();

        AllergenTranslation translation;
        if (existingOpt.isPresent()) {
            translation = existingOpt.get();
            translation.setName(name);
            translation.setDescription(description);
            log.info("Updated translation for allergen {} in {}", allergen.getCode(), languageCode);
        } else {
            translation = new AllergenTranslation();
            translation.setAllergen(allergen);
            translation.setLanguageCode(languageCode);
            translation.setName(name);
            translation.setDescription(description);
            log.info("Added translation for allergen {} in {}", allergen.getCode(), languageCode);
        }

        translation = translationRepository.save(translation);
        clearCaches();
        return translation;
    }

    /**
     * Add or update a translation for a category.
     */
    public CategoryTranslation addOrUpdateCategoryTranslation(Long categoryId, String languageCode, String name) {
        AllergenCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        // Check if translation already exists
        Optional<CategoryTranslation> existingOpt = categoryTranslationRepository.findAll().stream()
                .filter(t -> t.getCategory().getId().equals(categoryId) &&
                            t.getLanguageCode().equals(languageCode))
                .findFirst();

        CategoryTranslation translation;
        if (existingOpt.isPresent()) {
            translation = existingOpt.get();
            translation.setName(name);
            log.info("Updated translation for category {} in {}", category.getCode(), languageCode);
        } else {
            translation = new CategoryTranslation();
            translation.setCategory(category);
            translation.setLanguageCode(languageCode);
            translation.setName(name);
            log.info("Added translation for category {} in {}", category.getCode(), languageCode);
        }

        translation = categoryTranslationRepository.save(translation);
        clearCaches();
        return translation;
    }

    // ==================== KEYWORD MANAGEMENT ====================

    /**
     * Add a keyword for an allergen in a specific language.
     */
    public AllergenKeyword addKeyword(Long allergenId, String languageCode, String keyword) {
        Allergen allergen = allergenRepository.findById(allergenId)
                .orElseThrow(() -> new IllegalArgumentException("Allergen not found: " + allergenId));

        AllergenKeyword kw = new AllergenKeyword();
        kw.setAllergen(allergen);
        kw.setLanguageCode(languageCode);
        kw.setKeyword(keyword.toLowerCase());

        kw = keywordRepository.save(kw);
        log.info("Added keyword '{}' for allergen {} in {}", keyword, allergen.getCode(), languageCode);

        // Refresh keyword cache in matching service
        matchingService.refreshKeywords();
        return kw;
    }

    /**
     * Delete a keyword.
     */
    public void deleteKeyword(Long keywordId) {
        AllergenKeyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new IllegalArgumentException("Keyword not found: " + keywordId));

        keywordRepository.delete(keyword);
        log.info("Deleted keyword: {}", keyword.getKeyword());

        // Refresh keyword cache in matching service
        matchingService.refreshKeywords();
    }

    /**
     * Get all keywords for an allergen.
     */
    @Transactional(readOnly = true)
    public List<AllergenKeyword> getKeywordsForAllergen(Long allergenId) {
        Allergen allergen = allergenRepository.findById(allergenId)
                .orElseThrow(() -> new IllegalArgumentException("Allergen not found: " + allergenId));
        return keywordRepository.findByAllergen(allergen);
    }

    // ==================== CATEGORY MANAGEMENT ====================

    /**
     * Create a new allergen category.
     */
    public AllergenCategory createCategory(String code, String icon, Integer displayOrder) {
        AllergenCategory category = new AllergenCategory();
        category.setCode(code);
        category.setIcon(icon);
        category.setDisplayOrder(displayOrder);

        category = categoryRepository.save(category);
        log.info("Created new category: {}", code);

        clearCaches();
        return category;
    }

    /**
     * Update a category.
     */
    public AllergenCategory updateCategory(Long categoryId, String icon, Integer displayOrder) {
        AllergenCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        if (icon != null) {
            category.setIcon(icon);
        }
        if (displayOrder != null) {
            category.setDisplayOrder(displayOrder);
        }

        category = categoryRepository.save(category);
        log.info("Updated category: {}", category.getCode());

        clearCaches();
        return category;
    }

    // ==================== CACHE MANAGEMENT ====================

    /**
     * Clear all allergen-related caches.
     */
    public void clearCaches() {
        log.info("Clearing allergen caches");

        if (cacheManager.getCache("allergens") != null) {
            cacheManager.getCache("allergens").clear();
        }
        if (cacheManager.getCache("allergensByCategory") != null) {
            cacheManager.getCache("allergensByCategory").clear();
        }
        if (cacheManager.getCache("allergen") != null) {
            cacheManager.getCache("allergen").clear();
        }
        if (cacheManager.getCache("categories") != null) {
            cacheManager.getCache("categories").clear();
        }
    }

    /**
     * Refresh all caches by clearing and reloading keywords.
     */
    public void refreshAllCaches() {
        clearCaches();
        matchingService.refreshKeywords();
        log.info("Refreshed all allergen caches");
    }
}
