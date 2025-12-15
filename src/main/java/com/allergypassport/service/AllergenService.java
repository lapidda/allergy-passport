package com.allergypassport.service;

import com.allergypassport.entity.Allergen;
import com.allergypassport.entity.AllergenCategory;
import com.allergypassport.repository.AllergenCategoryRepository;
import com.allergypassport.repository.AllergenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing allergens and allergen categories.
 * Provides business logic for allergen CRUD operations with caching support.
 */
@Service
@Transactional
public class AllergenService {

    private static final Logger log = LoggerFactory.getLogger(AllergenService.class);

    private final AllergenRepository allergenRepository;
    private final AllergenCategoryRepository categoryRepository;

    public AllergenService(AllergenRepository allergenRepository,
                          AllergenCategoryRepository categoryRepository) {
        this.allergenRepository = allergenRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Get all allergens ordered by category and display order.
     * Results are cached to improve performance.
     * Uses JOIN FETCH to eagerly load categories.
     * Deduplicates by ID to avoid duplicates from JOIN FETCH with translations.
     *
     * @return List of all allergens in proper display order
     */
    @Cacheable("allergens")
    public List<Allergen> getAllAllergens() {
        log.debug("Fetching all allergens from database");
        List<Allergen> allergens = allergenRepository.findAllWithCategoryOrderByDisplayOrder();
        // Deduplicate by ID while preserving order (JOIN FETCH causes duplicates due to translations)
        return deduplicateById(allergens);
    }

    /**
     * Get all allergens grouped by category.
     * Results are cached to improve performance.
     * Uses JOIN FETCH to eagerly load categories and avoid LazyInitializationException.
     * Deduplicates by ID and uses LinkedHashMap to preserve category order.
     *
     * @return Map of allergen categories to their allergens
     */
    @Cacheable("allergensByCategory")
    public Map<AllergenCategory, List<Allergen>> getAllergensByCategory() {
        log.debug("Fetching allergens grouped by category");
        List<Allergen> allergens = allergenRepository.findAllWithCategoryOrderByDisplayOrder();
        // Deduplicate by ID while preserving order
        List<Allergen> uniqueAllergens = deduplicateById(allergens);
        // Use LinkedHashMap to preserve category order
        return uniqueAllergens.stream()
                .collect(Collectors.groupingBy(
                        Allergen::getCategory,
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    /**
     * Deduplicate allergens by ID while preserving order.
     * This is needed because JOIN FETCH with one-to-many relationships causes duplicate rows.
     */
    private List<Allergen> deduplicateById(List<Allergen> allergens) {
        return new ArrayList<>(allergens.stream()
                .collect(Collectors.toMap(
                        Allergen::getId,
                        a -> a,
                        (a1, a2) -> a1,  // keep first occurrence
                        LinkedHashMap::new))
                .values());
    }

    /**
     * Find an allergen by its unique code.
     * Results are cached to improve performance.
     *
     * @param code the allergen code (e.g., "PEANUTS", "APPLE")
     * @return Optional containing the allergen if found
     */
    @Cacheable(value = "allergen", key = "#code")
    public Optional<Allergen> findByCode(String code) {
        log.debug("Finding allergen by code: {}", code);
        return allergenRepository.findByCode(code);
    }

    /**
     * Get an allergen by ID.
     *
     * @param id the allergen ID
     * @return the allergen
     * @throws IllegalArgumentException if allergen not found
     */
    public Allergen getById(Long id) {
        log.debug("Finding allergen by ID: {}", id);
        return allergenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Allergen not found with ID: " + id));
    }

    /**
     * Get all allergen categories ordered by display order.
     * Results are cached to improve performance.
     *
     * @return List of all categories in display order
     */
    @Cacheable("categories")
    public List<AllergenCategory> getAllCategories() {
        log.debug("Fetching all allergen categories");
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * Find a category by its unique code.
     *
     * @param code the category code (e.g., "LEGALLY_MANDATED", "FRUITS")
     * @return Optional containing the category if found
     */
    public Optional<AllergenCategory> findCategoryByCode(String code) {
        log.debug("Finding category by code: {}", code);
        return categoryRepository.findByCode(code);
    }

    /**
     * Get all legally mandated allergens (EU top 14).
     *
     * @return List of legally mandated allergens
     */
    public List<Allergen> getLegallyMandatedAllergens() {
        log.debug("Fetching legally mandated allergens");
        return allergenRepository.findByIsLegallyMandatedTrue();
    }

    /**
     * Get allergens for a specific category.
     *
     * @param category the allergen category
     * @return List of allergens in that category
     */
    public List<Allergen> getAllergensByCategory(AllergenCategory category) {
        log.debug("Fetching allergens for category: {}", category.getCode());
        return allergenRepository.findByCategoryOrderByDisplayOrderAsc(category);
    }
}
