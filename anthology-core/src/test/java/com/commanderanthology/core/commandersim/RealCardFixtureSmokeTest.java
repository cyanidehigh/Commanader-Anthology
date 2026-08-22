package com.commanderanthology.core.commandersim;

import com.commanderanthology.core.fixtures.RealCardFixture;
import com.commanderanthology.core.fixtures.RealCardFixtureLoader;

import java.util.List;

public final class RealCardFixtureSmokeTest {
    private RealCardFixtureSmokeTest() {
    }

    public static void main(String[] args) {
        List<RealCardFixture> fixtures = RealCardFixtureLoader.loadKeywordFixtures();

        RealCardFixture lightningGreaves = RealCardFixtureLoader.requireByName(fixtures, "Lightning Greaves");
        assertEquals("{2}", lightningGreaves.manaCost(), "Lightning Greaves mana cost should come from real card data");
        assertTrue(lightningGreaves.hasKeyword("Equip"), "Lightning Greaves should expose Equip as a keyword reference");
        assertContains(lightningGreaves.oracleText(), "haste and shroud", "Lightning Greaves should retain real rules text");
        assertTrue(lightningGreaves.power().isEmpty(), "Lightning Greaves should not pretend to have power");

        RealCardFixture swiftfootBoots = RealCardFixtureLoader.requireByName(fixtures, "Swiftfoot Boots");
        assertTrue(swiftfootBoots.hasKeyword("Equip"), "Swiftfoot Boots should expose Equip as a keyword reference");
        assertContains(swiftfootBoots.oracleText(), "hexproof and haste", "Swiftfoot Boots should retain granted keywords text");

        RealCardFixture akrasanSquire = RealCardFixtureLoader.requireByName(fixtures, "Akrasan Squire");
        assertTrue(akrasanSquire.hasKeyword("Exalted"), "Akrasan Squire should expose Exalted as a keyword reference");
        assertEquals("1", akrasanSquire.power().orElseThrow(), "Akrasan Squire power should come from real card data");
        assertEquals("1", akrasanSquire.toughness().orElseThrow(), "Akrasan Squire toughness should come from real card data");

        RealCardFixture battlegraceAngel = RealCardFixtureLoader.requireByName(fixtures, "Battlegrace Angel");
        assertTrue(battlegraceAngel.hasKeyword("Flying"), "Battlegrace Angel should expose Flying");
        assertTrue(battlegraceAngel.hasKeyword("Exalted"), "Battlegrace Angel should expose Exalted");
        assertEquals("4", battlegraceAngel.power().orElseThrow(), "Battlegrace Angel power should come from real card data");
        assertEquals("4", battlegraceAngel.toughness().orElseThrow(), "Battlegrace Angel toughness should come from real card data");

        RealCardFixture rafiq = RealCardFixtureLoader.requireByName(fixtures, "Rafiq of the Many");
        assertTrue(rafiq.typeLine().contains("Legendary Creature"), "Rafiq should preserve real legendary creature type line");
        assertTrue(rafiq.hasKeyword("Exalted"), "Rafiq should expose Exalted as a keyword reference");
        assertContains(rafiq.oracleText(), "double strike", "Rafiq should retain bespoke triggered ability text");
        assertEquals("legal", rafiq.commanderLegality(), "Rafiq should preserve Commander legality from real data");

        System.out.println("Real card fixture smoke test passed.");
    }

    private static void assertContains(String value, String expectedPart, String message) {
        if (!value.contains(expectedPart)) {
            throw new AssertionError(message + " expected to contain=" + expectedPart + " actual=" + value);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }
}
