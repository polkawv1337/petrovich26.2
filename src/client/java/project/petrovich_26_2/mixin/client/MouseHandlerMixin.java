package project.petrovich_26_2.mixin.client;

import client_files.ClientikUtils.BindSetting;
import client_files.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onButton", at = @At("HEAD"))
    private void petrovich$mouseBinds(long windowPointer, MouseButtonInfo button, int action, CallbackInfo ci) {
        if (action != 1) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null && mc.gui.screen() != null) {
            return;
        }
        ModuleManager.getInstance().onKeyPress(BindSetting.MOUSE_BASE + button.button(), action);
    }

    @Inject(method = "onScroll", at = @At("HEAD"))
    private void petrovich$wheelBinds(long window, double xOffset, double yOffset, CallbackInfo ci) {
        if (yOffset == 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null && mc.gui.screen() != null) {
            return;
        }
        ModuleManager moduleManager = ModuleManager.getInstance();
        moduleManager.onScroll(yOffset > 0 ? 1 : 2);
        moduleManager.onKeyPress(yOffset > 0 ? BindSetting.SCROLL_UP : BindSetting.SCROLL_DOWN, 1);
    }
}