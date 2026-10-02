package client_files.Petrovich.Player;

import client_files.ClientikUtils.Category;
import client_files.Module;
import net.minecraft.client.player.LocalPlayer;

public class NoDelay extends Module {

    public NoDelay() {
        super("NoDelay", "Убирает задержку атаки при зажатой ЛКМ", Category.PLAYER);
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (mc.options.keyAttack.isDown() && player.getAttackStrengthScale(1.0f) < 1.0f) {
            player.resetAttackStrengthTicker();
        }
    }
}