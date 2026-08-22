package com.commanderanthology.core.commandersim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;

public final class Zone {
    private final int zoneId;
    private final ZoneType zoneType;
    private final OptionalInt ownerId;
    private final ArrayList<Integer> objectIds = new ArrayList<>();

    Zone(int zoneId, ZoneType zoneType, OptionalInt ownerId) {
        this.zoneId = zoneId;
        this.zoneType = zoneType;
        this.ownerId = ownerId;
    }

    public int zoneId() {
        return zoneId;
    }

    public ZoneType zoneType() {
        return zoneType;
    }

    public OptionalInt ownerId() {
        return ownerId;
    }

    public List<Integer> objectIds() {
        return List.copyOf(objectIds);
    }

    void addObject(int objectId) {
        objectIds.add(objectId);
    }

    void removeObject(int objectId) {
        objectIds.remove(Integer.valueOf(objectId));
    }

    void shuffle(Random random) {
        Collections.shuffle(objectIds, random);
    }
}
