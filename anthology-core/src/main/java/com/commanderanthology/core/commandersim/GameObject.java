package com.commanderanthology.core.commandersim;

import java.util.Objects;
import java.util.Optional;

public final class GameObject {
    private final int objectId;
    private final int entityId;
    private final String name;
    private final int ownerId;
    private final int controllerId;
    private final Optional<CardKind> cardKind;
    private final int manaCost;
    private final int power;
    private final int toughness;
    private final boolean commander;
    private final boolean legendary;
    private ObjectType objectType;
    private int zoneId;
    private boolean tapped;
    private boolean summoningSick;

    GameObject(
            int objectId,
            int entityId,
            String name,
            ObjectType objectType,
            int ownerId,
            int controllerId,
            int zoneId,
            Optional<CardKind> cardKind,
            int manaCost,
            int power,
            int toughness,
            boolean tapped,
            boolean summoningSick,
            boolean commander,
            boolean legendary
    ) {
        this.objectId = objectId;
        this.entityId = entityId;
        this.name = Objects.requireNonNull(name, "name");
        this.objectType = Objects.requireNonNull(objectType, "objectType");
        this.ownerId = ownerId;
        this.controllerId = controllerId;
        this.zoneId = zoneId;
        this.cardKind = Objects.requireNonNull(cardKind, "cardKind");
        this.manaCost = manaCost;
        this.power = power;
        this.toughness = toughness;
        this.tapped = tapped;
        this.summoningSick = summoningSick;
        this.commander = commander;
        this.legendary = legendary;
    }

    public int objectId() { return objectId; }
    public int entityId() { return entityId; }
    public String name() { return name; }
    public ObjectType objectType() { return objectType; }
    public int ownerId() { return ownerId; }
    public int controllerId() { return controllerId; }
    public int zoneId() { return zoneId; }
    public Optional<CardKind> cardKind() { return cardKind; }
    public int manaCost() { return manaCost; }
    public int power() { return power; }
    public int toughness() { return toughness; }
    public boolean tapped() { return tapped; }
    public boolean summoningSick() { return summoningSick; }
    public boolean commander() { return commander; }
    public boolean legendary() { return legendary; }

    void moveTo(int zoneId, ObjectType objectType, boolean tapped, boolean summoningSick) {
        this.zoneId = zoneId;
        this.objectType = objectType;
        this.tapped = tapped;
        this.summoningSick = summoningSick;
    }
}
