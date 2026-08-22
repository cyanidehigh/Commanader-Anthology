package com.commanderanthology.desktop;

public final class ScryfallCacheStatusSmokeTest {
    private ScryfallCacheStatusSmokeTest() {
    }

    public static void main(String[] args) {
        ScryfallCardLookupService lookup = new ScryfallCardLookupService();
        require(!lookup.cacheStatuses().isEmpty(), "cache statuses discovered");
        require(lookup.cacheStatuses().stream().anyMatch(ScryfallCacheStatus::hasSqlite), "sqlite cache discovered");
        require(lookup.cacheStatuses().stream().anyMatch(ScryfallCacheStatus::hasDefaultCards), "default cards discovered");
        require(lookup.lookupCardOptions("Sol Ring", null).stream().anyMatch(selection -> "Sol Ring".equals(selection.oracleName())), "local lookup still works");
        System.out.println("Scryfall cache status smoke test passed: " + lookup.cacheStatuses().get(0).summary());
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
