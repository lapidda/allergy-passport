package com.allergypassport.dto;

/**
 * DTO for allergen data to send to frontend.
 * Contains localized allergen information.
 */
public class AllergenDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isLegallyMandated;
    private Integer displayOrder;
    private Long categoryId;
    private String categoryCode;

    public AllergenDTO() {
    }

    public AllergenDTO(Long id, String code, String name, String description,
                       Boolean isLegallyMandated, Integer displayOrder,
                       Long categoryId, String categoryCode) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.isLegallyMandated = isLegallyMandated;
        this.displayOrder = displayOrder;
        this.categoryId = categoryId;
        this.categoryCode = categoryCode;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsLegallyMandated() {
        return isLegallyMandated;
    }

    public void setIsLegallyMandated(Boolean legallyMandated) {
        isLegallyMandated = legallyMandated;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }
}
