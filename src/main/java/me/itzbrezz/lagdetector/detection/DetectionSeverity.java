package me.itzbrezz.lagdetector.detection;

public enum DetectionSeverity {

    LOW(1, "LOW"),
    MEDIUM(2, "MEDIUM"),
    HIGH(3, "HIGH"),
    CRITICAL(4, "CRITICAL");

    private final int level;
    private final String displayName;

    DetectionSeverity(
            int level,
            String displayName
    ) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAtLeast(
            DetectionSeverity other
    ) {
        if (other == null) {
            return true;
        }

        return level >= other.level;
    }

    public boolean isCritical() {
        return this == CRITICAL;
    }

    public boolean isHighOrAbove() {
        return level >= HIGH.level;
    }

    public static DetectionSeverity fromLevel(
            int level
    ) {
        for (DetectionSeverity severity :
                values()) {

            if (severity.level == level) {
                return severity;
            }
        }

        if (level <= LOW.level) {
            return LOW;
        }

        return CRITICAL;
    }
}
