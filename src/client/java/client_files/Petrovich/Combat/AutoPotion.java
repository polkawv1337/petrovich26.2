package client_files.Petrovich.Combat;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.SliderSetting;
import client_files.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoPotion extends Module {

    private final SliderSetting health = addSetting(new SliderSetting("Порог здоровья", 8.0f, 2.0f, 16.0f, 1.0f));

    private boolean started;
    private int timer;

    public AutoPotion() {
        super("AutoPotion", "Бросает сплэш-зелье при низком здоровье", Category.COMBAT);
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (player.getHealth() > health.getValue()) {
            started = false;
            return;
        }
        if (timer > 0) {
            timer--;
            return;
        }

        ItemStack main = player.getMainHandItem();
        if (main.getItem() != Items.SPLASH_POTION) {
            int slot = findPotion(player.getInventory());
            if (slot == -1) return;
            player.getInventory().setSelectedSlot(slot);
            started = false;
            timer = 4;
            return;
        }
        if (player.isUsingItem()) return;

        if (!started) {
            player.startUsingItem(InteractionHand.MAIN_HAND);
            started = true;
            timer = 2;
        } else {
            player.stopUsingItem();
            started = false;
            timer = 40;
        }
    }

    private int findPotion(Inventory inventory) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == Items.SPLASH_POTION) {
                return i;
            }
        }
        return -1;
    }
}