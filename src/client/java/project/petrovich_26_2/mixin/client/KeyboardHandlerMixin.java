package project.petrovich_26_2.mixin.client;

import client_files.ModuleManager;
import client_files.Petrovich.Movement.InventoryMove;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void petrovich$onKeyPress(long windowPointer, int action, KeyEvent event, CallbackInfo ci) {
        if (action == 1) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui == null || mc.gui.screen() == null) {
                ModuleManager.getInstance().onKeyPress(event.key(), action);
            }
        }
        petrovich$inventoryMove(action, event);
    }

    @Unique
    private static void petrovich$inventoryMove(int action, KeyEvent event) {
        if (!InventoryMove.shouldForce()) {
            return;
        }
        if (action != 0 && action != 1) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Screen screen = mc.gui.screen();
        if (screen == null) {
            return;
        }
        if (screen.getFocused() instanceof EditBox editBox && editBox.canConsumeInput()) {
            return;
        }
        InputConstants.Key key = InputConstants.getKey(event);
        if (petrovich$isMovementKey(mc.options, key)) {
            KeyMapping.set(key, action == 1);
        }
    }

    @Unique
    private static boolean petrovich$isMovementKey(Options options, InputConstants.Key key) {
        return options.keyUp.matches(key)
                || options.keyDown.matches(key)
                || options.keyLeft.matches(key)
                || options.keyRight.matches(key)
                || options.keyJump.matches(key)
                || options.keyShift.matches(key)
                || options.keySprint.matches(key);
    }
}