package com.commanderanthology.core.commandersim;

import com.commanderanthology.core.move.StructuredMoveToken;
import com.commanderanthology.core.validation.ValidationGate;

import java.time.Instant;

public record GameEvent(
        Instant occurredAt,
        ValidationGate gate,
        StructuredMoveToken move,
        String detail
) {
}
