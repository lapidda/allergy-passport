package com.allergypassport.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entity representing an individual allergen (e.g., Peanuts, Apple, Fructose).
 * Allergens are organized into categories and have multi-language translations and keywords.
 */
@Entity
@Table(name = "allergens")
public class Allergen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private AllergenCategory category;

    @Column(length = 10)
    private String emoji;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_legally_mandated")
    private Boolean isLegallyMandated = false;

    @OneToMany(mappedBy = "allergen", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<AllergenTranslation> translations = new HashSet<>();

    @OneToMany(mappedBy = "allergen", cascade = CascadeType.ALL)
    private Set<AllergenKeyword> keywords = new HashSet<>();

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
     * Helper method to get translated allergen name for a specific language.
     *
     * @param languageCode the language code (e.g., "en", "de", "zh")
     * @return the translated name, or the code if no translation exists
     */
    public String getName(String languageCode) {
        return translations.stream()
                .filter(t -> t.getLanguageCode().equals(languageCode))
                .findFirst()
                .map(AllergenTranslation::getName)
                .orElse(code);
    }

    /**
     * Helper method to get OCR keywords for a specific language.
     *
     * @param languageCode the language code (e.g., "en", "de", "zh")
     * @return list of keywords for this allergen in the specified language
     */
    public List<String> getKeywords(String languageCode) {
        return keywords.stream()
                .filter(k -> k.getLanguageCode().equals(languageCode))
                .map(AllergenKeyword::getKeyword)
                .collect(Collectors.toList());
    }

    // Constructors

    public Allergen() {
    }

    public Allergen(String code, AllergenCategory category, String emoji, Integer displayOrder, Boolean isLegallyMandated) {
        this.code = code;
        this.category = category;
        this.emoji = emoji;
        this.displayOrder = displayOrder;
        this.isLegallyMandated = isLegallyMandated;
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

    public AllergenCategory getCategory() {
        return category;
    }

    public void setCategory(AllergenCategory category) {
        this.category = category;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsLegallyMandated() {
        return isLegallyMandated;
    }

    public void setIsLegallyMandated(Boolean isLegallyMandated) {
        this.isLegallyMandated = isLegallyMandated;
    }

    public Set<AllergenTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(Set<AllergenTranslation> translations) {
        this.translations = translations;
    }

    public Set<AllergenKeyword> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<AllergenKeyword> keywords) {
        this.keywords = keywords;
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
