package client_files.Petrovich.Player;

import client_files.ClientikUtils.BindSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.ChatHelper;
import client_files.ClientikUtils.InventoryUtil;
import client_files.ClientikUtils.SliderSetting;
import client_files.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;

public class ClickSetting extends Module {

    private final BindSetting addFriendKey = addSetting(new BindSetting("Добавить друга", -1));
    private final BindSetting pearlKey = addSetting(new BindSetting("Эндер жемчуг", -1));

    private int cooldown;

    public ClickSetting() {
        super("ClickSetting", "", Category.PLAYER);
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!mc.options.keyAttack.isDown()) {
            cooldown = 0;
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target) {
            if (target == player || !target.isAlive()) return;
            player.attack(target);
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public void onKeyPress(int key, int action) {
        if (action != 1) return;
        if (key == addFriendKey.getValue()) toggleFriendUnderCursor();
        if (key == pearlKey.getValue()) usePearl();
    }

    private void toggleFriendUnderCursor() {
        if (mc.player == null || mc.level == null) return;
        if (!(mc.crosshairPickEntity instanceof Player target)) return;
        if (target == mc.player) return;
        String name = target.getName().getString();
        if (FriendHelper.isFriend(name)) {
            FriendHelper.removeFriend(name);
            ChatHelper.print("Игрок \u00a7e" + name + "\u00a7f удалён из друзей");
        } else {
            FriendHelper.addFriend(name);
            ChatHelper.print("Игрок \u00a7e" + name + "\u00a7f добавлен в друзья");
        }
    }

    private void usePearl() {
        if (mc.player == null || mc.gameMode == null) return;

        if (mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL) {
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            return;
        }
        if (mc.player.getOffhandItem().getItem() == Items.ENDER_PEARL) {
            mc.gameMode.useItem(mc.player, InteractionHand.OFF_HAND);
            return;
        }

        int hotbarSlot = InventoryUtil.searchItemHotbar(Items.ENDER_PEARL);
        if (hotbarSlot != -1) {
            int previous = mc.player.getInventory().getSelectedSlot();
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(hotbarSlot));
            mc.player.getInventory().setSelectedSlot(hotbarSlot);
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            mc.player.getInventory().setSelectedSlot(previous);
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(previous));
            return;
        }

        int slot = InventoryUtil.searchItem(Items.ENDER_PEARL);
        if (slot == -1) {
            ChatHelper.print("Эндер жемчуг не найден");
            return;
        }

        int previous = mc.player.getInventory().getSelectedSlot();
        int target = 8;
        for (int i = 0; i < 8; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                target = i;
                break;
            }
        }

        int containerSlot = slot >= 9 ? slot : slot + 36;
        InventoryUtil.clickSlotNoSync(0, containerSlot, target, ContainerInput.SWAP, mc.player);
        mc.getConnection().send(new ServerboundContainerClosePacket(0));
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(target));
        mc.player.getInventory().setSelectedSlot(target);
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        mc.player.getInventory().setSelectedSlot(previous);
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(previous));
        InventoryUtil.clickSlotNoSync(0, containerSlot, target, ContainerInput.SWAP, mc.player);
        mc.getConnection().send(new ServerboundContainerClosePacket(0));
    }
}