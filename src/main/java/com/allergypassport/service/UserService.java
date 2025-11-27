package com.allergypassport.service;

import com.allergypassport.entity.AllergySeverity;
import com.allergypassport.entity.AllergyType;
import com.allergypassport.entity.User;
import com.allergypassport.entity.UserAllergy;
import com.allergypassport.repository.UserAllergyRepository;
import com.allergypassport.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing user data and allergies.
 */
@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserAllergyRepository userAllergyRepository;

    public UserService(UserRepository userRepository, UserAllergyRepository userAllergyRepository) {
        this.userRepository = userRepository;
        this.userAllergyRepository = userAllergyRepository;
    }

    /**
     * Find user by their database ID.
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find user by their public ID (for public passport view).
     */
    @Transactional(readOnly = true)
    public Optional<User> findByPublicId(String publicId) {
        return userRepository.findByPublicIdWithAllergies(publicId);
    }

    /**
     * Find user by Google ID with allergies loaded.
     */
    @Transactional(readOnly = true)
    public Optional<User> findByGoogleIdWithAllergies(String googleId) {
        return userRepository.findByGoogleIdWithAllergies(googleId);
    }

    /**
     * Update user profile information.
     */
    public User updateProfile(Long userId, String displayName, String bio) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (displayName != null && !displayName.isBlank()) {
            user.setDisplayName(displayName.trim());
        }
        user.setBio(bio != null ? bio.trim() : null);

        log.info("Updated profile for user {}", userId);
        return userRepository.save(user);
    }

    /**
     * Upload a profile picture for the user.
     */
    public User uploadProfilePicture(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Store as byte array (for simplicity - consider cloud storage for production)
        user.setProfilePicture(file.getBytes());
        user.setProfilePictureContentType(contentType);

        log.info("Updated profile picture for user {}", userId);
        return userRepository.save(user);
    }

    /**
     * Delete user's custom profile picture (reverts to Google picture).
     */
    public User deleteProfilePicture(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setProfilePicture(null);
        user.setProfilePictureContentType(null);

        log.info("Deleted profile picture for user {}", userId);
        return userRepository.save(user);
    }

    /**
     * Get all allergies for a user.
     */
    @Transactional(readOnly = true)
    public List<UserAllergy> getUserAllergies(Long userId) {
        return userAllergyRepository.findByUserId(userId);
    }

    /**
     * Add or update an allergy for a user.
     */
    public UserAllergy saveAllergy(Long userId, AllergyType allergyType, AllergySeverity severity, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Check if allergy already exists
        Optional<UserAllergy> existing = userAllergyRepository.findByUserIdAndAllergyType(userId, allergyType);

        UserAllergy allergy;
        if (existing.isPresent()) {
            allergy = existing.get();
            allergy.setSeverity(severity);
            allergy.setNotes(notes != null ? notes.trim() : null);
            log.info("Updated allergy {} for user {}", allergyType, userId);
        } else {
            allergy = new UserAllergy(user, allergyType, severity, notes != null ? notes.trim() : null);
            log.info("Added allergy {} for user {}", allergyType, userId);
        }

        return userAllergyRepository.save(allergy);
    }

    /**
     * Remove an allergy from a user.
     */
    public void removeAllergy(Long userId, AllergyType allergyType) {
        userAllergyRepository.deleteByUserIdAndAllergyType(userId, allergyType);
        log.info("Removed allergy {} for user {}", allergyType, userId);
    }

    /**
     * Remove an allergy by its ID.
     */
    public void removeAllergyById(Long userId, Long allergyId) {
        UserAllergy allergy = userAllergyRepository.findById(allergyId)
                .orElseThrow(() -> new IllegalArgumentException("Allergy not found: " + allergyId));

        if (!allergy.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Allergy does not belong to user");
        }

        userAllergyRepository.delete(allergy);
        log.info("Removed allergy {} for user {}", allergyId, userId);
    }

    /**
     * Update notes for an existing allergy.
     */
    public UserAllergy updateAllergyNotes(Long userId, Long allergyId, String notes) {
        UserAllergy allergy = userAllergyRepository.findById(allergyId)
                .orElseThrow(() -> new IllegalArgumentException("Allergy not found: " + allergyId));

        if (!allergy.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Allergy does not belong to user");
        }

        allergy.setNotes(notes != null ? notes.trim() : null);
        log.info("Updated notes for allergy {} of user {}", allergyId, userId);
        return userAllergyRepository.save(allergy);
    }
}
