package com.allergypassport.dto;

import com.allergypassport.entity.AllergySeverity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating or updating an allergy entry.
 */
public record AllergyRequest(
        @NotNull(message = "Allergen ID is required")
        Long allergenId,

        @NotNull(message = "Severity is required")
        AllergySeverity severity,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        String notes
) {
}
