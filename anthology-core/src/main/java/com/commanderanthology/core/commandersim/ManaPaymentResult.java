package com.commanderanthology.core.commandersim;

public record ManaPaymentResult(boolean payable, String reason) {
    public static ManaPaymentResult paid() {
        return new ManaPaymentResult(true, "");
    }

    public static ManaPaymentResult unpayable(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Unpayable mana result must explain why.");
        }
        return new ManaPaymentResult(false, reason);
    }
}
