package client_files.render.mixin;

import client_files.render.render.core.context.RenderContext;
import client_files.render.render.event.EventManager;
import client_files.render.render.event.impl.HudRenderEvent;
import client_files.render.render.event.impl.Render2DEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class MixinHud {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("TAIL")
    )
    private void Render$onExtractRenderState(GuiGraphicsExtractor guiGraphicsExtractor,
                                                DeltaTracker deltaTracker,
                                                CallbackInfo ci) {
        RenderContext.enter2D(guiGraphicsExtractor, deltaTracker);
        try {

            client_files.render.render.core.batching.BatchManager.get().beginFrame();

            if (EventManager.hasListeners(HudRenderEvent.class)) {
                EventManager.call(new HudRenderEvent(this.minecraft, guiGraphicsExtractor, deltaTracker));
            }
            if (EventManager.hasListeners(Render2DEvent.class)) {
                EventManager.call(new Render2DEvent(this.minecraft, guiGraphicsExtractor, deltaTracker));
            }

            client_files.render.render.particle.ParticleSystem.get().render(guiGraphicsExtractor, deltaTracker);

            client_files.render.render.core.batching.BatchManager.get().flush();
        } finally {
            RenderContext.exit2D();
        }
    }
}
