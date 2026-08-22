package com.commanderanthology.desktop;

record CommanderValidation(
        boolean valid,
        String cardName,
        String message
) {
    static CommanderValidation valid(String cardName) {
        return new CommanderValidation(true, cardName, "");
    }

    static CommanderValidation invalid(String cardName, String message) {
        return new CommanderValidation(false, cardName, message);
    }
}
