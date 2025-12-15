package com.allergypassport.repository;

import com.allergypassport.entity.Allergen;
import com.allergypassport.entity.AllergenTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for AllergenTranslation entities.
 * Provides database operations for allergen name translations.
 */
@Repository
public interface AllergenTranslationRepository extends JpaRepository<AllergenTranslation, Long> {

    /**
     * Find translations for a specific allergen and language.
     *
     * @param allergen the allergen entity
     * @param languageCode the language code (e.g., "en", "de", "zh")
     * @return List of translations (should be at most one due to unique constraint)
     */
    List<AllergenTranslation> findByAllergenAndLanguageCode(Allergen allergen, String languageCode);

    /**
     * Find a single translation for an allergen in a specific language.
     *
     * @param allergen the allergen entity
     * @param languageCode the language code
     * @return Optional containing the translation if found
     */
    Optional<AllergenTranslation> findOneByAllergenAndLanguageCode(Allergen allergen, String languageCode);

    /**
     * Find all translations for a specific language.
     * Useful for pre-loading all translations for a language.
     *
     * @param languageCode the language code
     * @return List of all allergen translations in that language
     */
    List<AllergenTranslation> findByLanguageCode(String languageCode);
}
