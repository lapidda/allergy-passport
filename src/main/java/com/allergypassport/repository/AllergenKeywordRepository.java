package com.allergypassport.repository;

import com.allergypassport.entity.Allergen;
import com.allergypassport.entity.AllergenKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AllergenKeyword entities.
 * Provides database operations for allergen OCR keywords.
 */
@Repository
public interface AllergenKeywordRepository extends JpaRepository<AllergenKeyword, Long> {

    /**
     * Find all keywords for a specific allergen and language.
     * Used for OCR allergen detection.
     *
     * @param allergen the allergen entity
     * @param languageCode the language code (e.g., "en", "de", "zh")
     * @return List of keywords for this allergen in the specified language
     */
    List<AllergenKeyword> findByAllergenAndLanguageCode(Allergen allergen, String languageCode);

    /**
     * Find all keywords for a specific allergen.
     *
     * @param allergen the allergen entity
     * @return List of all keywords for this allergen across all languages
     */
    List<AllergenKeyword> findByAllergen(Allergen allergen);

    /**
     * Find all keywords for a specific language.
     * Used for pre-loading all keywords for OCR in a specific language.
     *
     * @param languageCode the language code
     * @return List of all allergen keywords in that language
     */
    List<AllergenKeyword> findByLanguageCode(String languageCode);
}
