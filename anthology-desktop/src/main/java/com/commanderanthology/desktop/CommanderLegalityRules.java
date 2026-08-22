package com.commanderanthology.desktop;

import java.util.Locale;

final class CommanderLegalityRules {
    private CommanderLegalityRules() {
    }

    static boolean canBeCommander(String typeLine, String oracleText, String power, String toughness) {
        String type = normalized(typeLine);
        String text = normalized(oracleText);
        return (type.contains("legendary") && type.contains("creature"))
                || text.contains("can be your commander")
                || isEdgeOfEternitiesArtifactCommander(type, power, toughness);
    }

    private static boolean isEdgeOfEternitiesArtifactCommander(String type, String power, String toughness) {
        return type.contains("legendary")
                && (type.contains("vehicle") || type.contains("spacecraft"))
                && hasPrintedPowerToughness(power, toughness);
    }

    private static boolean hasPrintedPowerToughness(String power, String toughness) {
        return power != null && !power.isBlank() && toughness != null && !toughness.isBlank();
    }

    private static String normalized(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
