package com.commanderanthology.core.commandersim;

import java.util.OptionalInt;

public record PriorityResult(
        PriorityEvent event,
        OptionalInt priorityPlayerId,
        OptionalInt resolvedObjectId
) {
    public static PriorityResult passed(int nextPriorityPlayerId) {
        return new PriorityResult(PriorityEvent.PASSED, OptionalInt.of(nextPriorityPlayerId), OptionalInt.empty());
    }

    public static PriorityResult windowClosed() {
        return new PriorityResult(PriorityEvent.WINDOW_CLOSED, OptionalInt.empty(), OptionalInt.empty());
    }

    public static PriorityResult stackResolved(int nextPriorityPlayerId, int resolvedObjectId) {
        return new PriorityResult(PriorityEvent.STACK_RESOLVED, OptionalInt.of(nextPriorityPlayerId), OptionalInt.of(resolvedObjectId));
    }
}
