package client_files.render.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import client_files.render.render.core.context.RenderContext;
import client_files.render.render.event.EventManager;
import client_files.render.render.event.impl.Render3DEvent;
import client_files.render.render.event.impl.WorldRenderEvent;
import client_files.render.render.event.util.EventPhase;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {

    @Inject(method = "render", at = @At("HEAD"))
    private void Render$onLevelRenderHead(GraphicsResourceAllocator graphicsResourceAllocator,
                                            DeltaTracker deltaTracker,
                                            boolean renderBlockOutline,
                                            CameraRenderState cameraRenderState,
                                            Matrix4fc projectionMatrix,
                                            GpuBufferSlice fogParameters,
                                            Vector4f skyColor,
                                            boolean hasCapturedFrustum,
                                            CallbackInfo ci) {
        RenderContext.captureCameraState(cameraRenderState);
    }

    @Inject(
            method = "submitFeatures",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V"
            )
    )
    private void Render$onSubmitFeaturesGizmos(LevelRenderState levelRenderState,
                                                  SubmitNodeCollector submitNodeCollector,
                                                  boolean bl,
                                                  CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        CameraRenderState cameraState = levelRenderState.cameraRenderState;
        DeltaTracker delta = client.getDeltaTracker();

        RenderContext.enter3D(cameraState, null, delta);
        try {
            if (EventManager.hasListeners(WorldRenderEvent.class)) {
                EventManager.call(new WorldRenderEvent(client, cameraState, EventPhase.POST));
            }
            if (EventManager.hasListeners(Render3DEvent.class)) {
                EventManager.call(new Render3DEvent(client, cameraState, delta));
            }
        } finally {
            RenderContext.exit3D();
        }
    }

    @Inject(method = "submitEntities", at = @At("HEAD"))
    private void Render$onSubmitEntities(PoseStack poseStack,
                                            LevelRenderState levelRenderState,
                                            SubmitNodeCollector submitNodeCollector,
                                            CallbackInfo ci) {
        client_files.render.render.core.context.RenderContext.captureEntityRenderStates(levelRenderState);
    }
}
