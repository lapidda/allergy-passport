package com.allergypassport.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a user's specific allergy with severity and custom notes.
 * This is the "join" entity between User and AllergyType with additional attributes.
 */
@Entity
@Table(name = "user_allergies", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "allergy_type"}))
public class UserAllergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The type of allergy from the predefined list.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "allergy_type", nullable = false)
    private AllergyType allergyType;

    /**
     * Severity level: INTOLERANCE or SEVERE (anaphylactic).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllergySeverity severity;

    /**
     * Custom notes for this allergy (e.g., "Traces are okay", "Cook separately").
     */
    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors

    public UserAllergy() {
    }

    public UserAllergy(User user, AllergyType allergyType, AllergySeverity severity) {
        this.user = user;
        this.allergyType = allergyType;
        this.severity = severity;
    }

    public UserAllergy(User user, AllergyType allergyType, AllergySeverity severity, String notes) {
        this(user, allergyType, severity);
        this.notes = notes;
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

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AllergyType getAllergyType() {
        return allergyType;
    }

    public void setAllergyType(AllergyType allergyType) {
        this.allergyType = allergyType;
    }

    public AllergySeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AllergySeverity severity) {
        this.severity = severity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAllergy that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
