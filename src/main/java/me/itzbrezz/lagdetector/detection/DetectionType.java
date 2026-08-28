package me.itzbrezz.lagdetector.detection;

public enum DetectionType {

    REDSTONE,
    ENTITY,
    CHUNK,
    TICK,
    UNKNOWN;

    public String getDisplayName() {
        return switch (this) {
            case REDSTONE -> "REDSTONE";
            case ENTITY -> "ENTITY";
            case CHUNK -> "CHUNK";
            case TICK -> "TICK";
            case UNKNOWN -> "UNKNOWN";
        };
    }
        }
