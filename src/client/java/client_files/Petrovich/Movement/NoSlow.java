package client_files.Petrovich.Movement;

import client_files.ClientikUtils.Category;
import client_files.Module;
import net.minecraft.client.player.LocalPlayer;

public class NoSlow extends Module {

    public NoSlow() {
        super("NoSlow", "Убирает замедление при использовании предметов (лук/щит/еда)", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null || !player.isUsingItem()) return;
        if (player.getLastSentInput().forward() || player.getLastSentInput().backward() || player.getLastSentInput().left() || player.getLastSentInput().right()) {
            player.setSprinting(true);
        }
    }
}