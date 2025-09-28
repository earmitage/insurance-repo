package co.za.insurance.policy;


public enum PolicyType {

    LIFE_INSURANCE("Life Insurance"),
    HEALTH_INSURANCE("Health Insurance"),
    AUTO_INSURANCE("Auto Insurance"),
    HOME_INSURANCE("Home Insurance"),
    TRAVEL_INSURANCE("Travel Insurance"),
    DISABILITY_INSURANCE("Disability Insurance"),
    TERM_LIFE("Term Life"),
    WHOLE_LIFE("Whole Life"),
    UNIVERSAL_LIFE("Universal Life");

    private final String displayName;

    // Constructor
    PolicyType(String displayName) {
        this.displayName = displayName;
    }

    // Getter for the display name
    public String getDisplayName() {
        return displayName;
    }

    // Optional: Override toString() to return the display name
    @Override
    public String toString() {
        return displayName;
    }

    // Method to get enum from string value (useful for parsing)
    public static PolicyType fromString(String text) {
        for (PolicyType policyType : PolicyType.values()) {
            if (policyType.displayName.equalsIgnoreCase(text)) {
                return policyType;
            }
        }
        throw new IllegalArgumentException("No enum constant for: " + text);
    }
}

