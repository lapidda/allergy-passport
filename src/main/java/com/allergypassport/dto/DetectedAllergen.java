package com.allergypassport.dto;

import com.allergypassport.entity.Allergen;
import com.allergypassport.entity.AllergySeverity;

/**
 * DTO representing a detected allergen in scanned text.
 *
 * @param allergen The allergen detected
 * @param matchedKeyword The specific keyword that triggered the match
 * @param userSeverity The severity level this allergen has for the user
 */
public record DetectedAllergen(
        Allergen allergen,
        String matchedKeyword,
        AllergySeverity userSeverity
) {}
