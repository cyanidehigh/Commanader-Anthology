package com.commanderanthology.core.commandersim;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ManaPool {
    private static final List<ManaType> GENERIC_PAYMENT_ORDER = List.of(
            ManaType.COLORLESS,
            ManaType.WHITE,
            ManaType.BLUE,
            ManaType.BLACK,
            ManaType.RED,
            ManaType.GREEN
    );

    private final EnumMap<ManaType, Integer> mana = new EnumMap<>(ManaType.class);

    public ManaPool() {
        for (ManaType manaType : ManaType.values()) {
            mana.put(manaType, 0);
        }
    }

    public int white() {
        return amount(ManaType.WHITE);
    }

    public int blue() {
        return amount(ManaType.BLUE);
    }

    public int black() {
        return amount(ManaType.BLACK);
    }

    public int red() {
        return amount(ManaType.RED);
    }

    public int green() {
        return amount(ManaType.GREEN);
    }

    public int colorless() {
        return amount(ManaType.COLORLESS);
    }

    public int amount(ManaType manaType) {
        return mana.get(Objects.requireNonNull(manaType, "manaType"));
    }

    public int total() {
        return mana.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean canPayGeneric(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Mana amount cannot be negative.");
        }
        return total() >= amount;
    }

    public boolean canPay(ManaType manaType, int amount) {
        Objects.requireNonNull(manaType, "manaType");
        if (amount < 0) {
            throw new IllegalArgumentException("Mana amount cannot be negative.");
        }
        return this.amount(manaType) >= amount;
    }

    void add(ManaType manaType, int amount) {
        Objects.requireNonNull(manaType, "manaType");
        if (amount <= 0) {
            throw new IllegalArgumentException("Mana amount must be positive.");
        }
        mana.put(manaType, mana.get(manaType) + amount);
    }

    void payGeneric(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Mana amount cannot be negative.");
        }
        if (!canPayGeneric(amount)) {
            throw new IllegalStateException("Not enough mana in pool.");
        }
        int remaining = amount;
        for (ManaType manaType : GENERIC_PAYMENT_ORDER) {
            if (remaining == 0) {
                break;
            }
            int available = mana.get(manaType);
            int payment = Math.min(available, remaining);
            mana.put(manaType, available - payment);
            remaining -= payment;
        }
    }

    void pay(ManaType manaType, int amount) {
        Objects.requireNonNull(manaType, "manaType");
        if (amount < 0) {
            throw new IllegalArgumentException("Mana amount cannot be negative.");
        }
        if (!canPay(manaType, amount)) {
            throw new IllegalStateException("Not enough " + manaType.name().toLowerCase() + " mana in pool.");
        }
        mana.put(manaType, mana.get(manaType) - amount);
    }

    void empty() {
        for (ManaType manaType : ManaType.values()) {
            mana.put(manaType, 0);
        }
    }

    public Map<ManaType, Integer> asMap() {
        return Map.copyOf(mana);
    }
}
