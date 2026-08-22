package com.commanderanthology.desktop;

record ScryfallBulkDataStatus(
        String type,
        String name,
        String remoteUpdatedAt,
        String localUpdatedAt,
        long size,
        boolean installed,
        boolean updateAvailable,
        String downloadUri
) {
    String stateLabel() {
        if (!installed) {
            return "Not installed";
        }
        return updateAvailable ? "Update available" : "Installed";
    }
}
