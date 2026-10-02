package project.petrovich_26_2.mixin.client;

import client_files.ModuleManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void petrovich$onTick(CallbackInfo ci) {
        ModuleManager.getInstance().onTick();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void petrovich$onUpdate(CallbackInfo ci) {
        ModuleManager.getInstance().onUpdate();
    }
}