package project.petrovich_26_2.mixin.client;

import client_files.Petrovich.Movement.FireworkBoost;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {

    @Shadow
    private LivingEntity attachedToEntity;

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 1.5D))
    private double petrovich$boostSpeed(double original) {
        return FireworkBoost.boostSpeed(this.attachedToEntity, (float) original);
    }
}