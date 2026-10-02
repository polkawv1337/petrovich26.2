package client_files.Petrovich.Render;

import client_files.ClientikUtils.Category;
import client_files.Module;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class FullBright extends Module {

    public FullBright() {
        super("FullBright", "Делает мир очень светлым, как при гамме", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        if (!mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 3));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }
}