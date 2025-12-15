package com.allergypassport.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a category of allergens (e.g., Fruits, Vegetables, Legally Mandated).
 * Used for organizing and grouping allergens in the UI.
 */
@Entity
@Table(name = "allergen_categories")
public class AllergenCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(length = 10)
    private String icon;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private Set<Allergen> allergens = new HashSet<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<CategoryTranslation> translations = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to get translated category name for a specific language.
     *
     * @param languageCode the language code (e.g., "en", "de", "zh")
     * @return the translated name, or the code if no translation exists
     */
    public String getName(String languageCode) {
        return translations.stream()
                .filter(t -> t.getLanguageCode().equals(languageCode))
                .findFirst()
                .map(CategoryTranslation::getName)
                .orElse(code);
    }

    // Constructors

    public AllergenCategory() {
    }

    public AllergenCategory(String code, Integer displayOrder, String icon) {
        this.code = code;
        this.displayOrder = displayOrder;
        this.icon = icon;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Set<Allergen> getAllergens() {
        return allergens;
    }

    public void setAllergens(Set<Allergen> allergens) {
        this.allergens = allergens;
    }

    public Set<CategoryTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(Set<CategoryTranslation> translations) {
        this.translations = translations;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
