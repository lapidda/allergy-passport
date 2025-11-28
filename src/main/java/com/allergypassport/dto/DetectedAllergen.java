package com.allergypassport.dto;

import com.allergypassport.entity.AllergyType;
import com.allergypassport.entity.AllergySeverity;

/**
 * DTO representing a detected allergen in scanned text.
 *
 * @param allergyType The type of allergen detected
 * @param matchedKeyword The specific keyword that triggered the match
 * @param userSeverity The severity level this allergen has for the user
 */
public record DetectedAllergen(
        AllergyType allergyType,
        String matchedKeyword,
        AllergySeverity userSeverity
) {}
