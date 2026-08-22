package com.commanderanthology.desktop;

public final class CommanderLegalityRulesSmokeTest {
    private CommanderLegalityRulesSmokeTest() {
    }

    public static void main(String[] args) {
        require(CommanderLegalityRules.canBeCommander(
                "Legendary Creature - Human Knight",
                null,
                "3",
                "3"
        ), "legendary creature commander");
        require(CommanderLegalityRules.canBeCommander(
                "Legendary Artifact - Spacecraft",
                "Station",
                "5",
                "5"
        ), "legendary Spacecraft with printed power/toughness commander");
        require(CommanderLegalityRules.canBeCommander(
                "Legendary Artifact - Vehicle",
                "Crew 3",
                "4",
                "4"
        ), "legendary Vehicle with printed power/toughness commander");
        require(!CommanderLegalityRules.canBeCommander(
                "Legendary Artifact - Spacecraft",
                "Station",
                null,
                null
        ), "Spacecraft without printed power/toughness is not commander by EOE rule");
        require(!CommanderLegalityRules.canBeCommander(
                "Artifact - Vehicle",
                "Crew 1",
                "2",
                "2"
        ), "nonlegendary Vehicle is not commander by EOE rule");
        require(!CommanderLegalityRules.canBeCommander(
                "Artifact",
                "{T}: Add {C}{C}.",
                null,
                null
        ), "Sol Ring remains rejected");
        System.out.println("Commander legality rules smoke test passed.");
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError("Failed check: " + label);
        }
    }
}
