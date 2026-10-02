package client_files.render.mixin;

import client_files.render.render.core.context.RenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void Render$onExtractRenderState(CameraRenderState cameraRenderState,
                                                float cameraEntityPartialTick,
                                                CallbackInfo ci) {
        RenderContext.captureCameraState(cameraRenderState);
    }
}
