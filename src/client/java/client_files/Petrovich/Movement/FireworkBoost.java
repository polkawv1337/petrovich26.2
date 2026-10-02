package client_files.Petrovich.Movement;

import net.minecraft.world.entity.LivingEntity;

public final class FireworkBoost {

    private static ElytraBooster booster;

    private FireworkBoost() {
    }

    public static void setBooster(ElytraBooster module) {
        booster = module;
    }

    public static float boostSpeed(LivingEntity boosted, float original) {
        if (booster != null && booster.isEnabled() && boosted != null) {
            return booster.computeBoost(boosted, original);
        }
        return original;
    }

    public static void notifyFlag() {
        if (booster != null && booster.isEnabled()) {
            booster.onFlagged();
        }
    }
}