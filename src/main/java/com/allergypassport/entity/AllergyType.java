package com.allergypassport.entity;

/**
 * Predefined list of common food allergies.
 * The message key is used for i18n translation.
 */
public enum AllergyType {
    
    PEANUTS("allergy.peanuts", "🥜"),
    TREE_NUTS("allergy.tree_nuts", "🌰"),
    GLUTEN("allergy.gluten", "🌾"),
    WHEAT("allergy.wheat", "🌾"),
    DAIRY("allergy.dairy", "🥛"),
    EGGS("allergy.eggs", "🥚"),
    SHELLFISH("allergy.shellfish", "🦐"),
    FISH("allergy.fish", "🐟"),
    SOY("allergy.soy", "🫘"),
    SESAME("allergy.sesame", "🌱"),
    MUSTARD("allergy.mustard", "🟡"),
    CELERY("allergy.celery", "🥬"),
    LUPIN("allergy.lupin", "🌸"),
    MOLLUSCS("allergy.molluscs", "🐚"),
    SULPHITES("allergy.sulphites", "🧪");

    private final String messageKey;
    private final String emoji;

    AllergyType(String messageKey, String emoji) {
        this.messageKey = messageKey;
        this.emoji = emoji;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getEmoji() {
        return emoji;
    }
}
