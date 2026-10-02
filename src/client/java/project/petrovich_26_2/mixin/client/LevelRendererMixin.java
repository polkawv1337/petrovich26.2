package project.petrovich_26_2.mixin.client;

import client_files.Petrovich.Combat.CrystalAura;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method = "submitEntities", at = @At("HEAD"))
    private void petrovich$renderCrystalAura(
            PoseStack poseStack,
            LevelRenderState levelRenderState,
            SubmitNodeCollector submitNodeCollector,
            CallbackInfo ci
    ) {
        CrystalAura.renderWorld(poseStack, submitNodeCollector);
    }
}