package client_files.render.mixin;

import client_files.render.render.animation.AnimationEngine;
import client_files.render.render.particle.ParticleSystem;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(method = "tick", at = @At("HEAD"))
    private void Render$tickAnimations(CallbackInfo ci) {
        AnimationEngine.get().tick();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void Render$tickParticles(CallbackInfo ci) {
        ParticleSystem.get().tick();
    }
}
