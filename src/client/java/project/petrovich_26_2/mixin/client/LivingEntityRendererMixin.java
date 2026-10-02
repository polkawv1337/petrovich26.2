package project.petrovich_26_2.mixin.client;

import client_files.ClientikUtils.PredictUtils;
import client_files.ClientikUtils.RotationUtil;
import client_files.Petrovich.Combat.Aura;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    private static final Minecraft mc = Minecraft.getInstance();

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void petrovich$visualElytraRotation(T livingEntity, S state, float tickDelta, CallbackInfo ci) {
        if (livingEntity != mc.player || !livingEntity.isFallFlying()) return;

        Aura aura = client_files.ModuleManager.getInstance().get(Aura.class);
        if (aura == null || !aura.isEnabled() || !aura.visualElytraRotation.getValue()) return;

        LivingEntity target = Aura.target;
        if (target == null || !target.isAlive() || !target.isFallFlying()) return;

        Vec3 playerPos = livingEntity.getPosition(tickDelta);
        Vec3 targetPos = target.getPosition(tickDelta);

        Vec3 targetLook = target.getViewVector(tickDelta).normalize();
        Vec3 targetToPlayer = playerPos.subtract(targetPos);

        double dot = targetToPlayer.dot(targetLook);

        Vec3 predict = PredictUtils.predict(target, aura.getPredictValue());
        double distToPredict = playerPos.distanceTo(predict);

        if (dot > 0.0 && distToPredict < 6.0) {
            Vec3 center = targetPos.add(0.0, target.getBbHeight() / 2.0, 0.0);
            var rotation = RotationUtil.to(playerPos, center);

            state.bodyRot = rotation.getYaw();
            state.yRot = 0.0F;
            state.xRot = rotation.getPitch();
        }
    }
}
