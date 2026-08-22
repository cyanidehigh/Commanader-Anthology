package com.commanderanthology.core.commandersim;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ManaCost {
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("\\{([^}]+)}");

    private final EnumMap<ManaType, Integer> required;
    private final int generic;
    private final boolean variableX;
    private final int snow;
    private final List<String> hybridSymbols;
    private final List<String> phyrexianSymbols;
    private final List<String> unsupportedSymbols;

    private ManaCost(
            EnumMap<ManaType, Integer> required,
            int generic,
            boolean variableX,
            int snow,
            List<String> hybridSymbols,
            List<String> phyrexianSymbols,
            List<String> unsupportedSymbols
    ) {
        this.required = new EnumMap<>(required);
        this.generic = generic;
        this.variableX = variableX;
        this.snow = snow;
        this.hybridSymbols = List.copyOf(hybridSymbols);
        this.phyrexianSymbols = List.copyOf(phyrexianSymbols);
        this.unsupportedSymbols = List.copyOf(unsupportedSymbols);
    }

    public static ManaCost empty() {
        return generic(0);
    }

    public static ManaCost generic(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Generic mana cost cannot be negative.");
        }
        EnumMap<ManaType, Integer> required = emptyRequiredMap();
        return new ManaCost(required, amount, false, 0, List.of(), List.of(), List.of());
    }

    public static ManaCost parse(String printedCost) {
        if (printedCost == null || printedCost.isBlank()) {
            return empty();
        }

        EnumMap<ManaType, Integer> required = emptyRequiredMap();
        ArrayList<String> hybridSymbols = new ArrayList<>();
        ArrayList<String> phyrexianSymbols = new ArrayList<>();
        ArrayList<String> unsupportedSymbols = new ArrayList<>();
        int generic = 0;
        int snow = 0;
        boolean variableX = false;

        Matcher matcher = SYMBOL_PATTERN.matcher(printedCost);
        int coveredUntil = 0;
        while (matcher.find()) {
            if (!printedCost.substring(coveredUntil, matcher.start()).isBlank()) {
                unsupportedSymbols.add(printedCost.substring(coveredUntil, matcher.start()).trim());
            }
            coveredUntil = matcher.end();

            String symbol = matcher.group(1).trim().toUpperCase();
            if (symbol.matches("\\d+")) {
                generic += Integer.parseInt(symbol);
            } else if ("X".equals(symbol)) {
                variableX = true;
            } else if ("S".equals(symbol)) {
                snow += 1;
            } else if (symbol.contains("/P")) {
                phyrexianSymbols.add(symbol);
            } else if (symbol.contains("/")) {
                hybridSymbols.add(symbol);
            } else {
                ManaType manaType = manaTypeForSymbol(symbol);
                if (manaType == null) {
                    unsupportedSymbols.add(symbol);
                } else {
                    required.put(manaType, required.get(manaType) + 1);
                }
            }
        }

        if (!printedCost.substring(coveredUntil).isBlank()) {
            unsupportedSymbols.add(printedCost.substring(coveredUntil).trim());
        }

        return new ManaCost(required, generic, variableX, snow, hybridSymbols, phyrexianSymbols, unsupportedSymbols);
    }

    public int generic() {
        return generic;
    }

    public int required(ManaType manaType) {
        return required.get(Objects.requireNonNull(manaType, "manaType"));
    }

    public Map<ManaType, Integer> requiredMana() {
        return Map.copyOf(required);
    }

    public boolean variableX() {
        return variableX;
    }

    public int snow() {
        return snow;
    }

    public List<String> hybridSymbols() {
        return hybridSymbols;
    }

    public List<String> phyrexianSymbols() {
        return phyrexianSymbols;
    }

    public List<String> unsupportedSymbols() {
        return unsupportedSymbols;
    }

    public boolean hasUnsupportedPaymentChoices() {
        return variableX || snow > 0 || !hybridSymbols.isEmpty() || !phyrexianSymbols.isEmpty() || !unsupportedSymbols.isEmpty();
    }

    public int fixedManaValue() {
        int total = generic;
        for (int amount : required.values()) {
            total += amount;
        }
        return total;
    }

    private static EnumMap<ManaType, Integer> emptyRequiredMap() {
        EnumMap<ManaType, Integer> required = new EnumMap<>(ManaType.class);
        for (ManaType manaType : ManaType.values()) {
            required.put(manaType, 0);
        }
        return required;
    }

    private static ManaType manaTypeForSymbol(String symbol) {
        return switch (symbol) {
            case "W" -> ManaType.WHITE;
            case "U" -> ManaType.BLUE;
            case "B" -> ManaType.BLACK;
            case "R" -> ManaType.RED;
            case "G" -> ManaType.GREEN;
            case "C" -> ManaType.COLORLESS;
            default -> null;
        };
    }
}
