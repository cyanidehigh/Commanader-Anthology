package com.commanderanthology.core.commandersim;

import java.util.EnumMap;
import java.util.Map;

public final class Player {
    private final int playerId;
    private final String name;
    private final EnumMap<ZoneType, Integer> zoneIds = new EnumMap<>(ZoneType.class);
    private final ManaPool manaPool = new ManaPool();
    private int life;
    private boolean lost;
    private int landsPlayedThisTurn;

    Player(int playerId, String name, int life) {
        this.playerId = playerId;
        this.name = name;
        this.life = life;
    }

    public int playerId() {
        return playerId;
    }

    public String name() {
        return name;
    }

    public int life() {
        return life;
    }

    public boolean lost() {
        return lost;
    }

    public int manaPool() {
        return manaPool.total();
    }

    public ManaPool manaPoolDetails() {
        return manaPool;
    }

    public int landsPlayedThisTurn() {
        return landsPlayedThisTurn;
    }

    public Map<ZoneType, Integer> zoneIds() {
        return Map.copyOf(zoneIds);
    }

    int zoneId(ZoneType zoneType) {
        return zoneIds.get(zoneType);
    }

    void putZone(ZoneType zoneType, int zoneId) {
        zoneIds.put(zoneType, zoneId);
    }

    void addMana(int amount) {
        manaPool.add(ManaType.COLORLESS, amount);
    }

    void addMana(ManaType manaType, int amount) {
        manaPool.add(manaType, amount);
    }

    void payMana(int amount) {
        manaPool.payGeneric(amount);
    }

    void payMana(ManaCost manaCost) {
        new ManaPaymentEngine().pay(manaPool, manaCost);
    }

    void changeLife(int amount) {
        life += amount;
    }

    void loseGame() {
        lost = true;
    }

    void emptyManaPool() {
        manaPool.empty();
    }

    void recordLandPlayed() {
        landsPlayedThisTurn += 1;
    }

    void resetTurnCounters() {
        landsPlayedThisTurn = 0;
    }
}
