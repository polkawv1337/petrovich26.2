package client_files.Petrovich.Player;

import client_files.ClientikUtils.BindSetting;
import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.ChatHelper;
import client_files.ClientikUtils.InventoryUtil;
import client_files.ClientikUtils.StopWatch;
import client_files.Module;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ElytraHelper extends Module {

    private static final int CHEST_ARMOR_SLOT = 6;

    private final BindSetting swapKey = addSetting(new BindSetting("Кнопка свапа", -1));
    private final BindSetting fireworkKey = addSetting(new BindSetting("Кнопка феерверка", -1));
    private final BooleanSetting autoFly = addSetting(new BooleanSetting("Авто взлёт", true));
    private final BooleanSetting autoJump = addSetting(new BooleanSetting("Авто прыжок", true));
    private final BooleanSetting equipChestplate = addSetting(new BooleanSetting("Снимать элитры при приземлении", false));
    private final BooleanSetting autoFirework = addSetting(new BooleanSetting("Авто фейерверк", false));
    private final BooleanSetting autoFireworkStart = addSetting(new BooleanSetting("Только при взлёте", false));

    private boolean fireworkUsed;
    private boolean swapped;
    private final StopWatch timerUtil = new StopWatch();
    private boolean hasFiredOnStart;
    private int tickswap;
    private boolean wasOnGround;
    private boolean wasInElytraFlight;

    public ElytraHelper() {
        super("Elytra Helper", "Помощник полёта на элитрах", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timerUtil.reset();
        hasFiredOnStart = false;
        fireworkUsed = false;
        swapped = false;
        tickswap = 0;
        wasOnGround = false;
        wasInElytraFlight = false;
        autoFireworkStart.setVisible(autoFirework.getValue());
    }

    @Override
    public void onKeyPress(int key, int action) {
        if (action != 1) return;
        if (key == swapKey.getValue()) swapped = true;
        if (key == fireworkKey.getValue()) fireworkUsed = true;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        autoFireworkStart.setVisible(autoFirework.getValue());
        if (tickswap > 0) tickswap--;

        if (swapped) {
            swapped = false;
            swap(mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA);
        }

        ItemStack chest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        boolean hasElytra = chest.getItem() == Items.ELYTRA;

        if (equipChestplate.getValue()) {
            if (mc.player.onGround() && !wasOnGround && wasInElytraFlight) {
                if (hasElytra) {
                    swap(true);
                    wasInElytraFlight = false;
                }
            }
            wasOnGround = mc.player.onGround();
        }

        if (hasElytra && mc.player.isFallFlying()) {
            wasInElytraFlight = true;
        }

        if (autoJump.getValue() && !mc.player.getAbilities().flying && mc.player.onGround() && hasElytra
                && !mc.options.keyJump.isDown() && !mc.player.isInLava() && !mc.player.isInWater()) {
            mc.player.jumpFromGround();
        }

        if (autoFly.getValue() && !mc.player.getAbilities().flying && !mc.player.isInLava()
                && !mc.player.onGround() && !mc.player.isFallFlying() && hasElytra && !mc.player.isInWater()) {
            mc.player.startFallFlying();
            mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player,
                    ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));

            if (autoFirework.getValue() && autoFireworkStart.getValue() && !hasFiredOnStart) {
                if (InventoryUtil.searchItem(Items.FIREWORK_ROCKET) != -1) {
                    useFirework();
                    hasFiredOnStart = true;
                } else {
                    ChatHelper.print("У вас не были найдены фейерверки");
                }
            }
        }

        if (mc.player.onGround() || mc.player.isInLava() || mc.player.isInWater()) {
            hasFiredOnStart = false;
        }

        if (mc.player.isFallFlying() && autoFirework.getValue() && !autoFireworkStart.getValue()
                && timerUtil.isReached(570L)) {
            if (InventoryUtil.searchItem(Items.FIREWORK_ROCKET) != -1) {
                useFirework();
            } else {
                ChatHelper.print("У вас не были найдены фейерверки");
            }
            timerUtil.reset();
        }

        if (fireworkUsed) {
            if (InventoryUtil.searchItem(Items.FIREWORK_ROCKET) != -1) {
                useFirework();
            } else if (mc.player.isFallFlying()) {
                ChatHelper.print("У вас не были найдены фейерверки");
            }
            fireworkUsed = false;
        }
    }

    public void swap(boolean toChestplate) {
        if (mc.player == null || mc.gameMode == null) return;

        int targetSlot = toChestplate
                ? InventoryUtil.findBestChestplateSlot()
                : InventoryUtil.findBestElytraSlot();

        if (targetSlot == -1) {
            return;
        }

        int containerSlot = targetSlot >= 9 ? targetSlot : targetSlot + 36;
        if (containerSlot == CHEST_ARMOR_SLOT) {
            return;
        }

        tickswap = 2;
        InventoryUtil.clickSlotNoSync(0, containerSlot, 0, ContainerInput.PICKUP, mc.player);
        InventoryUtil.clickSlotNoSync(0, CHEST_ARMOR_SLOT, 0, ContainerInput.PICKUP, mc.player);
        InventoryUtil.clickSlotNoSync(0, containerSlot, 0, ContainerInput.PICKUP, mc.player);
        mc.getConnection().send(new ServerboundContainerClosePacket(0));
    }

    private void useFirework() {
        if (mc.player == null || mc.gameMode == null) return;

        int slot = InventoryUtil.searchItem(Items.FIREWORK_ROCKET, 9, 45);
        int slotHotbar = InventoryUtil.searchItem(Items.FIREWORK_ROCKET, 0, 9);
        int previousSlot = mc.player.getInventory().getSelectedSlot();

        if (mc.player.getMainHandItem().getItem() == Items.FIREWORK_ROCKET) {
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            return;
        }
        if (mc.player.getOffhandItem().getItem() == Items.FIREWORK_ROCKET) {
            mc.gameMode.useItem(mc.player, InteractionHand.OFF_HAND);
            return;
        }

        if (slotHotbar != -1) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(slotHotbar));
            mc.player.getInventory().setSelectedSlot(slotHotbar);
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            mc.player.getInventory().setSelectedSlot(previousSlot);
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(previousSlot));
            return;
        }

        if (slot == -1) {
            return;
        }

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
        mc.player.getInventory().setSelectedSlot(previousSlot);
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(previousSlot));
        InventoryUtil.clickSlotNoSync(0, containerSlot, target, ContainerInput.SWAP, mc.player);
        mc.getConnection().send(new ServerboundContainerClosePacket(0));
    }

    @Override
    public void onDisable() {
        timerUtil.reset();
        hasFiredOnStart = false;
        fireworkUsed = false;
        swapped = false;
        tickswap = 0;
        wasOnGround = false;
        wasInElytraFlight = false;
    }
}