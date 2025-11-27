package com.allergypassport.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO for updating user profile information.
 */
public record ProfileRequest(
        @Size(max = 100, message = "Display name cannot exceed 100 characters")
        String displayName,

        @Size(max = 500, message = "Bio cannot exceed 500 characters")
        String bio
) {
}
