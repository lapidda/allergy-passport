package com.allergypassport.repository;

import com.allergypassport.entity.Allergen;
import com.allergypassport.entity.UserAllergy;
import com.allergypassport.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAllergyRepository extends JpaRepository<UserAllergy, Long> {

    /**
     * Find all allergies for a specific user.
     */
    List<UserAllergy> findByUserId(Long userId);

    /**
     * Find all allergies for a user, ordered by category and allergen display order.
     * Useful for displaying allergies grouped by category.
     */
    List<UserAllergy> findByUserIdOrderByAllergen_Category_DisplayOrderAscAllergen_DisplayOrderAsc(Long userId);

    /**
     * Find all allergies for a user with allergen and category eagerly fetched.
     * Uses JOIN FETCH to avoid LazyInitializationException when accessing allergen/category properties.
     */
    @Query("SELECT DISTINCT ua FROM UserAllergy ua " +
           "JOIN FETCH ua.allergen a " +
           "JOIN FETCH a.category c " +
           "WHERE ua.user.id = :userId " +
           "ORDER BY c.displayOrder ASC, a.displayOrder ASC")
    List<UserAllergy> findByUserIdWithAllergenAndCategory(@Param("userId") Long userId);

    /**
     * Find all allergies for a user entity.
     */
    List<UserAllergy> findByUser(User user);

    /**
     * Find a specific allergy entry for a user.
     */
    Optional<UserAllergy> findByUserIdAndAllergenId(Long userId, Long allergenId);

    /**
     * Find a specific allergy entry for a user by User and Allergen entities.
     */
    Optional<UserAllergy> findByUserAndAllergen(User user, Allergen allergen);

    /**
     * Check if a user has a specific allergy registered.
     */
    boolean existsByUserIdAndAllergenId(Long userId, Long allergenId);

    /**
     * Check if a user has a specific allergy registered by entities.
     */
    boolean existsByUserAndAllergen(User user, Allergen allergen);

    /**
     * Delete all allergies for a user.
     */
    @Modifying
    @Query("DELETE FROM UserAllergy ua WHERE ua.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    /**
     * Delete a specific allergy for a user.
     */
    @Modifying
    @Query("DELETE FROM UserAllergy ua WHERE ua.user.id = :userId AND ua.allergen.id = :allergenId")
    void deleteByUserIdAndAllergenId(@Param("userId") Long userId, @Param("allergenId") Long allergenId);

    /**
     * Delete a specific allergy for a user by entities.
     */
    void deleteByUserAndAllergen(User user, Allergen allergen);

    /**
     * Count allergies for a user.
     */
    long countByUserId(Long userId);
}
