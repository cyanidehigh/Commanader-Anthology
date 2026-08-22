package com.commanderanthology.core.card;

import java.util.Objects;
import java.util.Optional;

/**
 * Rules identity for a card. Scryfall oracle_id is preferred; fallback IDs are
 * typed so they cannot be mistaken for oracle IDs.
 */
public final class CardIdentity {
    private final String oracleId;
    private final String fallbackId;

    private CardIdentity(String oracleId, String fallbackId) {
        this.oracleId = oracleId;
        this.fallbackId = fallbackId;
    }

    public static CardIdentity oracle(String oracleId) {
        return new CardIdentity(requireText(oracleId, "oracleId"), null);
    }

    public static CardIdentity fallback(String fallbackId) {
        return new CardIdentity(null, requireText(fallbackId, "fallbackId"));
    }

    public Optional<String> oracleId() {
        return Optional.ofNullable(oracleId);
    }

    public Optional<String> fallbackId() {
        return Optional.ofNullable(fallbackId);
    }

    public boolean isOracleBacked() {
        return oracleId != null;
    }

    public String stableKey() {
        return isOracleBacked() ? "oracle:" + oracleId : "fallback:" + fallbackId;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CardIdentity that)) {
            return false;
        }
        return Objects.equals(oracleId, that.oracleId)
                && Objects.equals(fallbackId, that.fallbackId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oracleId, fallbackId);
    }

    @Override
    public String toString() {
        return stableKey();
    }
}

