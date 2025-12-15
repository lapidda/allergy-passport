package com.allergypassport.repository;

import com.allergypassport.entity.AllergenCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for AllergenCategory entities.
 * Provides database operations for allergen categories.
 */
@Repository
public interface AllergenCategoryRepository extends JpaRepository<AllergenCategory, Long> {

    /**
     * Find a category by its unique code.
     *
     * @param code the category code (e.g., "LEGALLY_MANDATED", "FRUITS")
     * @return Optional containing the category if found
     */
    Optional<AllergenCategory> findByCode(String code);

    /**
     * Find all categories ordered by display order for UI rendering.
     *
     * @return List of categories in display order
     */
    List<AllergenCategory> findAllByOrderByDisplayOrderAsc();
}
