package com.commanderanthology.core.commandersim;

import java.util.Objects;

public final class ManaPaymentEngine {
    public ManaPaymentResult evaluate(ManaPool pool, ManaCost cost) {
        Objects.requireNonNull(pool, "pool");
        Objects.requireNonNull(cost, "cost");

        if (cost.hasUnsupportedPaymentChoices()) {
            return ManaPaymentResult.unpayable("Mana cost contains unsupported choice symbols.");
        }

        int requiredTotal = 0;
        for (ManaType manaType : ManaType.values()) {
            int required = cost.required(manaType);
            requiredTotal += required;
            if (!pool.canPay(manaType, required)) {
                return ManaPaymentResult.unpayable("Missing required " + manaType.name().toLowerCase() + " mana.");
            }
        }

        int remainingAfterRequired = pool.total() - requiredTotal;
        if (remainingAfterRequired < cost.generic()) {
            return ManaPaymentResult.unpayable("Not enough mana remaining for generic cost.");
        }

        return ManaPaymentResult.paid();
    }

    public void pay(ManaPool pool, ManaCost cost) {
        ManaPaymentResult result = evaluate(pool, cost);
        if (!result.payable()) {
            throw new IllegalStateException(result.reason());
        }

        for (ManaType manaType : ManaType.values()) {
            int required = cost.required(manaType);
            if (required > 0) {
                pool.pay(manaType, required);
            }
        }
        pool.payGeneric(cost.generic());
    }
}
