package com.allergypassport.repository;

import com.allergypassport.entity.AllergyType;
import com.allergypassport.entity.UserAllergy;
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
     * Find a specific allergy entry for a user.
     */
    Optional<UserAllergy> findByUserIdAndAllergyType(Long userId, AllergyType allergyType);

    /**
     * Check if a user has a specific allergy registered.
     */
    boolean existsByUserIdAndAllergyType(Long userId, AllergyType allergyType);

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
    @Query("DELETE FROM UserAllergy ua WHERE ua.user.id = :userId AND ua.allergyType = :allergyType")
    void deleteByUserIdAndAllergyType(@Param("userId") Long userId, @Param("allergyType") AllergyType allergyType);

    /**
     * Count allergies for a user.
     */
    long countByUserId(Long userId);
}
