package com.allergypassport.repository;

import com.allergypassport.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by Google OAuth2 subject ID.
     */
    Optional<User> findByGoogleId(String googleId);

    /**
     * Find user by their public ID (used in public URLs).
     */
    Optional<User> findByPublicId(String publicId);

    /**
     * Find user by email address.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists with the given Google ID.
     */
    boolean existsByGoogleId(String googleId);

    /**
     * Find user with allergies eagerly loaded (for public view).
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.allergies WHERE u.publicId = :publicId")
    Optional<User> findByPublicIdWithAllergies(@Param("publicId") String publicId);

    /**
     * Find user by Google ID with allergies eagerly loaded.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.allergies WHERE u.googleId = :googleId")
    Optional<User> findByGoogleIdWithAllergies(@Param("googleId") String googleId);
}
