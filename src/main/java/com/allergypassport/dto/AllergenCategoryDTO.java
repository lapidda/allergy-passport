package com.allergypassport.dto;

import java.util.List;

/**
 * DTO for allergen category data to send to frontend.
 * Contains localized category information and associated allergens.
 */
public class AllergenCategoryDTO {

    private Long id;
    private String code;
    private String name;
    private Integer displayOrder;
    private String icon;
    private List<AllergenDTO> allergens;

    public AllergenCategoryDTO() {
    }

    public AllergenCategoryDTO(Long id, String code, String name, Integer displayOrder, String icon) {
        this.id = id;
        this.code = code;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<AllergenDTO> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<AllergenDTO> allergens) {
        this.allergens = allergens;
    }
}
