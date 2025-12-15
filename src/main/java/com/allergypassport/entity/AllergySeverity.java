package com.allergypassport.entity;

/**
 * Enum representing the severity level of an allergy.
 * Ranges from low intolerance to life-threatening severe reactions.
 */
public enum AllergySeverity {

    /**
     * Low intolerance - tolerable in small amounts.
     */
    INTOLERANCE_LOW("intolerance_low", "info", "⚠️", 1),

    /**
     * Mild intolerance - causes mild discomfort.
     */
    INTOLERANCE_MILD("intolerance_mild", "warning", "⚠️⚠️", 2),

    /**
     * Severe intolerance - causes severe discomfort.
     */
    INTOLERANCE_SEVERE("intolerance_severe", "warning", "⚠️⚠️⚠️", 3),

    /**
     * Life-threatening anaphylactic reaction.
     */
    SEVERE("severe", "danger", "☠️", 4);

    private final String code;
    private final String cssClass;
    private final String icon;
    private final int severityLevel;

    AllergySeverity(String code, String cssClass, String icon, int severityLevel) {
        this.code = code;
        this.cssClass = cssClass;
        this.icon = icon;
        this.severityLevel = severityLevel;
    }

    public String getCode() {
        return code;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getIcon() {
        return icon;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }
}
