package client_files.Petrovich.Combat;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.InventoryUtil;
import client_files.ClientikUtils.SliderSetting;
import client_files.ClientikUtils.StopWatch;
import client_files.Module;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

public class AutoArmor extends Module {

    private final SliderSetting delay = addSetting(new SliderSetting("Задержка", 150, 0, 1000, 5));
    private final BooleanSetting alwaysUpgrade = addSetting(new BooleanSetting("Всегда обновлять", true));
    private final BooleanSetting skipHelmet = addSetting(new BooleanSetting("Не свапать шлем", false));
    private final BooleanSetting skipBoots = addSetting(new BooleanSetting("Не свапать ботинки", false));

    private final StopWatch timer = new StopWatch();

    public AutoArmor() {
        super("AutoArmor", "Автоматически экипирует лучшую броню", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) return;
        Screen screen = mc.gui != null ? mc.gui.screen() : null;
        if (screen != null && !(screen instanceof InventoryScreen)) return;
        if (!timer.isReached((long) delay.getValue())) return;

        equip(EquipmentSlot.HEAD);
        equip(EquipmentSlot.CHEST);
        equip(EquipmentSlot.LEGS);
        equip(EquipmentSlot.FEET);
    }

    private void equip(EquipmentSlot equipSlot) {
        if (equipSlot == EquipmentSlot.HEAD && skipHelmet.getValue()) return;
        if (equipSlot == EquipmentSlot.FEET && skipBoots.getValue()) return;

        int bestSlot = InventoryUtil.getBestArmorSlot(equipSlot);
        if (bestSlot == -1) return;

        ItemStack current = mc.player.getItemBySlot(equipSlot);
        ItemStack candidate = mc.player.getInventory().getItem(bestSlot);

        if (!alwaysUpgrade.getValue() && !current.isEmpty()) return;
        if (alwaysUpgrade.getValue() && !current.isEmpty()) {
            double currentScore = InventoryUtil.getArmorScore(current);
            double candidateScore = InventoryUtil.getArmorScore(candidate);
            if (candidateScore <= currentScore) return;
        }

        mc.gameMode.handleContainerInput(0, bestSlot, 0, ContainerInput.QUICK_MOVE, mc.player);
        timer.reset();
    }
}