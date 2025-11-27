package com.allergypassport.entity;

/**
 * Enum representing the severity level of an allergy.
 */
public enum AllergySeverity {
    
    /**
     * Mild to moderate intolerance - causes discomfort but not life-threatening.
     */
    INTOLERANCE("intolerance", "warning", "⚠️"),
    
    /**
     * Severe/anaphylactic reaction - potentially life-threatening.
     */
    SEVERE("severe", "danger", "☠️");

    private final String code;
    private final String cssClass;
    private final String icon;

    AllergySeverity(String code, String cssClass, String icon) {
        this.code = code;
        this.cssClass = cssClass;
        this.icon = icon;
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
}
