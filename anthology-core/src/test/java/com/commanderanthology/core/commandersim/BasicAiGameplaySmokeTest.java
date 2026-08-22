package com.commanderanthology.core.commandersim;

import com.commanderanthology.core.move.MoveType;

import java.util.List;
import java.util.Optional;

public final class BasicAiGameplaySmokeTest {
    private BasicAiGameplaySmokeTest() {
    }

    public static void main(String[] args) {
        GameFoundation game = GameFoundation.buildGame(List.of("AI One", "AI Two"));
        int first = game.playerOrder().get(0);
        int second = game.playerOrder().get(1);
        seedOpeningHand(game, first, "Forest", "Grizzly Bears");
        seedOpeningHand(game, second, "Island", "Merfolk");
        seedLibrary(game, first, "Plains");
        seedLibrary(game, second, "Swamp");

        BasicAiGameDriver driver = new BasicAiGameDriver();
        driver.run(game, 40);

        assertTrue(
                game.objectsInZone(game.sharedZoneIds().get(ZoneType.BATTLEFIELD)).stream()
                        .anyMatch(object -> object.cardKind().orElse(null) == CardKind.LAND),
                "AI should play lands to the battlefield"
        );
        assertTrue(
                driver.rules().events().stream().anyMatch(event -> event.move().type() == MoveType.ACTIVATE_MANA_ABILITY),
                "AI should use generated mana ability moves"
        );
        assertTrue(
                driver.rules().events().stream().anyMatch(event -> event.move().type() == MoveType.CAST_SPELL),
                "AI should cast a legal supported spell"
        );
        game.validate();
        System.out.println("Basic AI gameplay smoke test passed.");
    }

    private static void seedOpeningHand(GameFoundation game, int playerId, String landName, String creatureName) {
        int hand = game.players().get(playerId).zoneIds().get(ZoneType.HAND);
        game.addObject(landName, ObjectType.CARD, playerId, hand, Optional.of(CardKind.LAND), 0, 0, 0);
        game.addObject(creatureName, ObjectType.CARD, playerId, hand, Optional.of(CardKind.CREATURE), 1, 2, 2);
    }

    private static void seedLibrary(GameFoundation game, int playerId, String landName) {
        int library = game.players().get(playerId).zoneIds().get(ZoneType.LIBRARY);
        game.addObject(landName, ObjectType.CARD, playerId, library, Optional.of(CardKind.LAND), 0, 0, 0);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
