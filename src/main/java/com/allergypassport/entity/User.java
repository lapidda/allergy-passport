package com.allergypassport.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User entity representing a registered user in the system.
 * Users authenticate via Google OAuth2 and can manage their allergy information.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique public identifier used in the public URL (/u/{publicId}).
     * Generated automatically on user creation.
     */
    @Column(name = "public_id", unique = true, nullable = false, updatable = false)
    private String publicId;

    /**
     * Google OAuth2 subject identifier (unique per Google account).
     */
    @Column(name = "google_id", unique = true, nullable = false)
    private String googleId;

    @Column(nullable = false)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    /**
     * User's short bio/description shown on the public passport.
     */
    @Column(length = 500)
    private String bio;

    /**
     * User's preferred language for the interface (e.g., "en", "de", "fr", "it", "es").
     * Defaults to "en" (English).
     */
    @Column(name = "preferred_language", length = 5)
    private String preferredLanguage = "en";

    /**
     * Profile picture stored as byte array.
     * For production, consider using cloud storage.
     */
    @Lob
    @Column(name = "profile_picture")
    private byte[] profilePicture;

    @Column(name = "profile_picture_content_type")
    private String profilePictureContentType;

    /**
     * URL to Google profile picture (fallback if no custom picture uploaded).
     */
    @Column(name = "google_picture_url")
    private String googlePictureUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * User's allergy entries.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserAllergy> allergies = new ArrayList<>();

    // Constructors

    public User() {
        this.publicId = UUID.randomUUID().toString().substring(0, 8);
    }

    public User(String googleId, String email, String displayName) {
        this();
        this.googleId = googleId;
        this.email = email;
        this.displayName = displayName;
    }

    // Lifecycle callbacks

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods

    public void addAllergy(UserAllergy allergy) {
        allergies.add(allergy);
        allergy.setUser(this);
    }

    public void removeAllergy(UserAllergy allergy) {
        allergies.remove(allergy);
        allergy.setUser(null);
    }

    /**
     * Returns the effective profile picture URL.
     * Uses custom uploaded picture if available, otherwise falls back to Google picture.
     */
    public boolean hasCustomProfilePicture() {
        return profilePicture != null && profilePicture.length > 0;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getProfilePictureContentType() {
        return profilePictureContentType;
    }

    public void setProfilePictureContentType(String profilePictureContentType) {
        this.profilePictureContentType = profilePictureContentType;
    }

    public String getGooglePictureUrl() {
        return googlePictureUrl;
    }

    public void setGooglePictureUrl(String googlePictureUrl) {
        this.googlePictureUrl = googlePictureUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<UserAllergy> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<UserAllergy> allergies) {
        this.allergies = allergies;
    }
}
