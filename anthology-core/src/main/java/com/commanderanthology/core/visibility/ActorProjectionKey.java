package com.commanderanthology.core.visibility;

public record ActorProjectionKey(String seatId, ActorType actorType) {
    public ActorProjectionKey {
        if (seatId == null || seatId.isBlank()) {
            throw new IllegalArgumentException("seatId is required");
        }
        if (actorType == null) {
            throw new IllegalArgumentException("actorType is required");
        }
    }

    public String parityKey() {
        return seatId;
    }
}

