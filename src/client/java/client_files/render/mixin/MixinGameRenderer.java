package client_files.render.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import client_files.render.render.event.EventManager;
import client_files.render.render.event.impl.CameraBobEvent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void Render$onBobHurt(CameraRenderState cameraRenderState, PoseStack poseStack, CallbackInfo ci) {
        if (!EventManager.hasListeners(CameraBobEvent.class)) {
            return;
        }
        CameraBobEvent event = new CameraBobEvent(cameraRenderState);
        EventManager.call(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
