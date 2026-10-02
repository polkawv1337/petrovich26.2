package client_files.Petrovich.Movement;

import client_files.ClientikUtils.Category;
import client_files.Module;
import client_files.ModuleManager;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;

public class InventoryMove extends Module {

    public InventoryMove() {
        super("InventoryMove", "Ходить и перекладывать предметы при открытом инвентаре", Category.MOVEMENT);
    }

    public static boolean shouldForce() {
        Module module = ModuleManager.getInstance().get(InventoryMove.class);
        return module != null && module.isEnabled();
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui == null) {
            return;
        }
        Screen screen = mc.gui.screen();
        if (screen == null) {
            return;
        }
        if (screen.getFocused() instanceof EditBox editBox && editBox.canConsumeInput()) {
            return;
        }

        List<KeyMapping> movement = movementKeys(mc.options);
        for (KeyMapping mapping : movement) {
            if (mapping.isUnbound()) {
                continue;
            }
            if (mapping == mc.options.keyShift) {
                continue;
            }
            InputConstants.Key key = ((project.petrovich_26_2.mixin.client.KeyMappingAccessor) mapping).petrovich$getKey();
            boolean down = InputConstants.isKeyDown(mc.getWindow(), key.getValue());
            KeyMapping.set(key, down);
        }

        InputConstants.Key shiftKey = ((project.petrovich_26_2.mixin.client.KeyMappingAccessor) mc.options.keyShift).petrovich$getKey();
        KeyMapping.set(shiftKey, false);

        LocalPlayer player = mc.player;
        if (player == null || player.isUsingItem() || player.isFallFlying() || mc.options.keyShift.isDown()) {
            return;
        }
        if (mc.options.keyUp.isDown() || mc.options.keyDown.isDown()
                || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown()) {
            player.setSprinting(true);
        }
    }

    private static List<KeyMapping> movementKeys(Options options) {
        List<KeyMapping> keys = new ArrayList<>();
        keys.add(options.keyUp);
        keys.add(options.keyDown);
        keys.add(options.keyLeft);
        keys.add(options.keyRight);
        keys.add(options.keyJump);
        keys.add(options.keyShift);
        keys.add(options.keySprint);
        return keys;
    }
}