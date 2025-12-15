package com.allergypassport.repository;

import com.allergypassport.entity.AllergenCategory;
import com.allergypassport.entity.CategoryTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CategoryTranslation entities.
 * Provides database operations for category name translations.
 */
@Repository
public interface CategoryTranslationRepository extends JpaRepository<CategoryTranslation, Long> {

    /**
     * Find a translation for a specific category and language.
     *
     * @param category the allergen category
     * @param languageCode the language code (e.g., "en", "de", "zh")
     * @return Optional containing the translation if found
     */
    Optional<CategoryTranslation> findByCategoryAndLanguageCode(AllergenCategory category, String languageCode);

    /**
     * Find all translations for a specific language.
     *
     * @param languageCode the language code
     * @return List of all category translations in that language
     */
    List<CategoryTranslation> findByLanguageCode(String languageCode);
}
