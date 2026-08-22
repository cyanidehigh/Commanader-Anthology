package com.commanderanthology.desktop;

record ScryfallCardDetails(
        String scryfallCardId,
        String oracleId,
        String name,
        String oracleName,
        String manaCost,
        String typeLine,
        String oracleText,
        String setName,
        String setCode,
        String collectorNumber,
        String rarity,
        String imageUrl
) {
    String title() {
        return name + " - " + setCode + " #" + collectorNumber;
    }
}
