package client_files.Petrovich.Movement;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.Module;
import client_files.ModuleManager;
import client_files.Petrovich.Combat.Aura;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;

public class AutoSprint extends Module {

    private final BooleanSetting sneakCancel = addSetting(new BooleanSetting("Выкл. при краже", true));

    public AutoSprint() {
        super("AutoSprint", "Автоматический спринт", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null) return;

        Aura aura = ModuleManager.getInstance().get(Aura.class);
        boolean microStop = aura != null && aura.isEnabled()
                && Aura.target != null && Aura.ticksToAttack <= 1
                && player.getAttackStrengthScale(1.0F) >= 0.95F;

        boolean blocked = (sneakCancel.getValue() && player.getLastSentInput().shift())
                || player.isUsingItem()
                || player.isFallFlying()
                || player.hasEffect(MobEffects.BLINDNESS)
                || !player.canSprint()
                || (player.isInWater() && !player.isUnderWater())
                || microStop;

        if (!blocked && player.getLastSentInput().forward()) {
            player.setSprinting(true);
        }
    }
}