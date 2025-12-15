package com.allergypassport.repository;

import com.allergypassport.entity.Allergen;
import com.allergypassport.entity.AllergenCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Allergen entities.
 * Provides database operations for allergens.
 */
@Repository
public interface AllergenRepository extends JpaRepository<Allergen, Long> {

    /**
     * Find an allergen by its unique code.
     *
     * @param code the allergen code (e.g., "PEANUTS", "APPLE", "FRUCTOSE")
     * @return Optional containing the allergen if found
     */
    Optional<Allergen> findByCode(String code);

    /**
     * Find all allergens in a specific category, ordered by display order.
     *
     * @param category the allergen category
     * @return List of allergens in the category
     */
    List<Allergen> findByCategoryOrderByDisplayOrderAsc(AllergenCategory category);

    /**
     * Find all allergens ordered by category display order and then allergen display order.
     * Used for displaying all allergens grouped by category.
     *
     * @return List of all allergens in proper display order
     */
    List<Allergen> findAllByOrderByCategory_DisplayOrderAscDisplayOrderAsc();

    /**
     * Find all allergens with category eagerly fetched, ordered by category and allergen display order.
     * Uses JOIN FETCH to avoid LazyInitializationException when accessing category properties.
     *
     * @return List of all allergens with categories fully loaded
     */
    @Query("SELECT DISTINCT a FROM Allergen a " +
           "JOIN FETCH a.category c " +
           "ORDER BY c.displayOrder ASC, a.displayOrder ASC")
    List<Allergen> findAllWithCategoryOrderByDisplayOrder();

    /**
     * Find all legally mandated allergens (EU top 14).
     *
     * @return List of legally mandated allergens
     */
    List<Allergen> findByIsLegallyMandatedTrue();
}
