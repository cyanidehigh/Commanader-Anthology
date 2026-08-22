package com.commanderanthology.core.commandersim;

import com.commanderanthology.core.move.StructuredMoveToken;
import com.commanderanthology.core.validation.ValidationGate;

public record LegalMove(
        StructuredMoveToken token,
        ValidationGate generatedBy
) {
    public LegalMove {
        if (generatedBy != ValidationGate.LEGAL_MOVE_GENERATION) {
            throw new IllegalArgumentException("Legal moves must come from the legal move generation gate.");
        }
    }
}
