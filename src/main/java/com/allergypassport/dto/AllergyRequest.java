package com.allergypassport.dto;

import com.allergypassport.entity.AllergySeverity;
import com.allergypassport.entity.AllergyType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating or updating an allergy entry.
 */
public record AllergyRequest(
        @NotNull(message = "Allergy type is required")
        AllergyType allergyType,

        @NotNull(message = "Severity is required")
        AllergySeverity severity,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        String notes
) {
}
