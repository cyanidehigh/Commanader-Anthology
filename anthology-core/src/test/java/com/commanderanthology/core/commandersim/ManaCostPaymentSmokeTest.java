package com.commanderanthology.core.commandersim;

public final class ManaCostPaymentSmokeTest {
    private ManaCostPaymentSmokeTest() {
    }

    public static void main(String[] args) {
        parsesFixedPrintedCosts();
        paysGenericWithAnyMana();
        requiresColorlessForColorlessSymbols();
        paysColoredAndColorlessBeforeGeneric();
        preservesUnsupportedChoiceSymbols();
        System.out.println("Mana cost/payment smoke test passed.");
    }

    private static void parsesFixedPrintedCosts() {
        ManaCost cost = ManaCost.parse("{1}{G}{W}{U}{C}");

        assertEquals(1, cost.generic(), "generic symbol should parse");
        assertEquals(1, cost.required(ManaType.GREEN), "green requirement should parse");
        assertEquals(1, cost.required(ManaType.WHITE), "white requirement should parse");
        assertEquals(1, cost.required(ManaType.BLUE), "blue requirement should parse");
        assertEquals(1, cost.required(ManaType.COLORLESS), "colorless requirement should parse");
        assertEquals(5, cost.fixedManaValue(), "fixed mana value should include generic and exact symbols");
        assertFalse(cost.hasUnsupportedPaymentChoices(), "fixed costs should not be flagged as choices");
    }

    private static void paysGenericWithAnyMana() {
        ManaPool pool = new ManaPool();
        pool.add(ManaType.WHITE, 1);
        pool.add(ManaType.BLUE, 1);

        ManaPaymentEngine engine = new ManaPaymentEngine();
        assertTrue(engine.evaluate(pool, ManaCost.parse("{2}")).payable(), "generic cost can use colored mana");
        engine.pay(pool, ManaCost.parse("{2}"));
        assertEquals(0, pool.total(), "generic payment should spend available mana");
    }

    private static void requiresColorlessForColorlessSymbols() {
        ManaPaymentEngine engine = new ManaPaymentEngine();
        ManaPool coloredOnly = new ManaPool();
        coloredOnly.add(ManaType.WHITE, 1);
        coloredOnly.add(ManaType.BLUE, 1);
        coloredOnly.add(ManaType.BLACK, 1);
        coloredOnly.add(ManaType.RED, 1);

        assertFalse(engine.evaluate(coloredOnly, ManaCost.parse("{C}")).payable(), "{C} cannot be paid by colored mana");
        assertFalse(engine.evaluate(coloredOnly, ManaCost.parse("{2}{C}{C}")).payable(), "{2}{C}{C} still needs two colorless mana");

        ManaPool withColorless = new ManaPool();
        withColorless.add(ManaType.WHITE, 1);
        withColorless.add(ManaType.BLUE, 1);
        withColorless.add(ManaType.COLORLESS, 2);
        assertTrue(engine.evaluate(withColorless, ManaCost.parse("{2}{C}{C}")).payable(), "{2}{C}{C} accepts two exact colorless plus any two generic mana");
    }

    private static void paysColoredAndColorlessBeforeGeneric() {
        ManaPool pool = new ManaPool();
        pool.add(ManaType.WHITE, 1);
        pool.add(ManaType.BLUE, 1);
        pool.add(ManaType.GREEN, 1);
        pool.add(ManaType.COLORLESS, 2);

        new ManaPaymentEngine().pay(pool, ManaCost.parse("{1}{G}{W}{C}"));

        assertEquals(0, pool.white(), "white exact cost should be spent");
        assertEquals(0, pool.green(), "green exact cost should be spent");
        assertEquals(1, pool.blue(), "unused blue should remain if colorless paid generic first");
        assertEquals(0, pool.colorless(), "colorless exact and generic payment should spend colorless");
        assertEquals(1, pool.total(), "one unused mana should remain");
    }

    private static void preservesUnsupportedChoiceSymbols() {
        ManaPaymentEngine engine = new ManaPaymentEngine();
        ManaPool pool = new ManaPool();
        pool.add(ManaType.GREEN, 3);
        pool.add(ManaType.BLUE, 3);

        ManaCost hybridX = ManaCost.parse("{X}{G/U}{G/U}");
        ManaCost phyrexian = ManaCost.parse("{W/P}");
        ManaCost snow = ManaCost.parse("{S}");

        assertTrue(hybridX.variableX(), "X should be represented explicitly");
        assertEquals(2, hybridX.hybridSymbols().size(), "hybrid symbols should be represented explicitly");
        assertEquals(1, phyrexian.phyrexianSymbols().size(), "Phyrexian symbols should be represented explicitly");
        assertEquals(1, snow.snow(), "snow symbols should be represented explicitly");
        assertFalse(engine.evaluate(pool, hybridX).payable(), "unsupported choice symbols should not be silently paid");
        assertFalse(engine.evaluate(pool, phyrexian).payable(), "Phyrexian symbols should not be silently paid");
        assertFalse(engine.evaluate(pool, snow).payable(), "snow symbols should not be silently paid");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }
}
