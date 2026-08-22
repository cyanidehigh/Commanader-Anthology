package com.commanderanthology.desktop;

import java.nio.file.Path;

record ScryfallCacheStatus(
        Path directory,
        boolean hasDefaultCards,
        boolean hasOracleCards,
        boolean hasRulings,
        boolean hasManifest,
        boolean hasSqlite,
        long defaultCardsBytes,
        long sqliteBytes
) {
    String summary() {
        String sqlite = hasSqlite ? formatBytes(sqliteBytes) + " SQLite" : "no SQLite";
        String bulk = hasDefaultCards ? formatBytes(defaultCardsBytes) + " default_cards" : "no default_cards";
        return directory + " - " + sqlite + " - " + bulk;
    }

    private static String formatBytes(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        double mib = bytes / 1024.0 / 1024.0;
        if (mib < 1024) {
            return String.format("%.1f MB", mib);
        }
        return String.format("%.2f GB", mib / 1024.0);
    }
}
