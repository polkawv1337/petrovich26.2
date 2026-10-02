package client_files.Petrovich.Combat;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public class TriggerBot extends Module {

    private final BooleanSetting onlyCritical = addSetting(new BooleanSetting("Только криты", true));
    private final BooleanSetting onlySpaceCritical = addSetting((BooleanSetting) new BooleanSetting("Только с пробелом", false)
            .setVisible(() -> onlyCritical.getValue()));

    private long cpsLimit;

    public TriggerBot() {
        super("TriggerBot", "Атакует как Aura, но нужно наводиться самостоятельно", Category.COMBAT);
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (cpsLimit > System.currentTimeMillis()) return;
        if (!(mc.hitResult instanceof EntityHitResult hit)) return;
        Entity ent = hit.getEntity();
        if (ent == null || !ent.isAlive()) return;
        if (ent == player || ent instanceof ArmorStand) return;
        if (ent instanceof Player p && client_files.Petrovich.Player.FriendHelper.isFriend(p.getGameProfile().name()))
            return;

        if (whenFalling()) {
            cpsLimit = System.currentTimeMillis() + 550;
            player.attack(ent);
            player.swing(InteractionHand.MAIN_HAND);
            ElytraLock.onPlayerAttack(ent);
        }
    }

    private boolean whenFalling() {
        LocalPlayer player = mc.player;
        if (player == null) return false;

        boolean critWater = player.isInWater();

        boolean reasonForCancelCritical = player.hasEffect(MobEffects.BLINDNESS)
                || player.onClimbable()
                || (player.isInWater() && critWater)
                || player.isPassenger()
                || player.getAbilities().flying
                || player.isFallFlying();

        final boolean onSpace = onlySpaceCritical.getValue()
                && player.onGround()
                && !mc.options.keyJump.isDown();

        if (player.getAttackStrengthScale(1.5F) < 0.92F)
            return false;

        if (!reasonForCancelCritical && onlyCritical.getValue()) {
            return onSpace || (!player.onGround() && player.fallDistance > 0.0F);
        }

        return true;
    }

    @Override
    public void onDisable() {
        cpsLimit = 0;
        super.onDisable();
    }
}