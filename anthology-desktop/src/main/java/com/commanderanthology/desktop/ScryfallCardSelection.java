package com.commanderanthology.desktop;

record ScryfallCardSelection(
        String scryfallCardId,
        String oracleId,
        String oracleName,
        String printingName,
        String setCode,
        String collectorNumber
) {
    String printingLabel() {
        return printingName + " - " + setCode.toUpperCase() + " #" + collectorNumber;
    }
}
