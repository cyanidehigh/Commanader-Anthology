package com.commanderanthology.core.commandersim;

import java.util.List;

public enum TurnStep {
    UNTAP,
    UPKEEP,
    DRAW,
    PRECOMBAT_MAIN,
    COMBAT,
    POSTCOMBAT_MAIN,
    END;

    public static final List<TurnStep> SEQUENCE = List.of(
            UNTAP,
            UPKEEP,
            DRAW,
            PRECOMBAT_MAIN,
            COMBAT,
            POSTCOMBAT_MAIN,
            END
    );
}
